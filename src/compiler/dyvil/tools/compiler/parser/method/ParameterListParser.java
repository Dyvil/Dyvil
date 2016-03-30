package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IParameterConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int TYPE      = 1;
	public static final int NAME      = 2;
	public static final int SEPARATOR = 4;

	// Flags

	private static final int VARARGS          = 1;
	public static final  int LAMBDA_ARROW_END = 2;

	protected IParameterConsumer consumer;

	// Metadata
	private ModifierList   modifiers;
	private AnnotationList annotations;

	private IType type;
	private int   flags;

	public ParameterListParser(IParameterConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = TYPE;
	}

	public ParameterListParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	private void reset()
	{
		this.modifiers = null;
		this.annotations = null;
		this.type = null;
		this.flags &= ~VARARGS;
	}

	private boolean hasFlag(int flag)
	{
		return (this.flags & flag) != 0;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		switch (this.mode)
		{
		case TYPE:
			if (type == BaseSymbols.SEMICOLON && token.isInferred())
			{
				return;
			}

			final Modifier modifier;
			if ((modifier = ModifierUtil.parseModifier(token, pm)) != null)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addModifier(modifier);
				return;
			}

			if (ParserUtil.isIdentifier(type))
			{
				final int nextType = token.next().type();
				if (ParserUtil.isTerminator(nextType) || nextType == DyvilSymbols.DOUBLE_ARROW_RIGHT && this.hasFlag(
					LAMBDA_ARROW_END))
				{
					// ... , IDENTIFIER , ...
					this.type = Types.UNKNOWN;
					this.mode = NAME;
					pm.reparse();
					return;
				}
			}

			if (type == DyvilSymbols.AT)
			{
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}

				final Annotation annotation = new Annotation(token.raw());
				this.annotations.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = NAME;
			pm.pushParser(new TypeParser(this), true);
			return;
		case NAME:
			switch (type)
			{
			case Tokens.EOF:
				pm.report(token, "parameter.identifier");
				pm.popParser();
				return;
			case DyvilSymbols.ELLIPSIS:
				if (this.hasFlag(VARARGS))
				{
					pm.report(token, "parameter.identifier");
					return;
				}
				this.flags |= VARARGS;
				return;
			case DyvilKeywords.THIS:
				this.mode = SEPARATOR;
				this.reset();
				if (this.consumer instanceof IParametric && !((IParametric) this.consumer).setReceiverType(this.type))
				{
					pm.report(token, "parameter.receivertype.invalid");
					return;
				}
				return;
			}

			this.mode = SEPARATOR;
			if (!ParserUtil.isIdentifier(type))
			{
				if (ParserUtil.isCloseBracket(type))
				{
					pm.popParser(true);
				}

				pm.report(token, "parameter.identifier");
				return;
			}

			if (this.hasFlag(VARARGS))
			{
				this.type = new ArrayType(this.type);
			}

			final IParameter parameter = this.createParameter(token);
			this.consumer.addParameter(parameter);

			final IToken next = token.next();
			switch (next.type())
			{
			case BaseSymbols.EQUALS:
				// ... IDENTIFIER = EXPRESSION ...

				pm.skip();
				pm.pushParser(new ExpressionParser(parameter));
				return;
			case BaseSymbols.COLON:
				// ... IDENTIFIER : TYPE ...
				if (this.type != Types.UNKNOWN)
				{
					pm.report(next, "parameter.type.duplicate");
				}

				pm.skip();
				pm.pushParser(new TypeParser(parameter));
				return;
			}

			this.reset();
			return;
		case SEPARATOR:
			this.mode = TYPE;
			this.reset();

			switch (type)
			{
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
				if (!this.hasFlag(LAMBDA_ARROW_END))
				{
					break; // produce a syntax error
				}
				// Fallthrough
			case BaseSymbols.CLOSE_PARENTHESIS:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
				pm.reparse();
				// Fallthrough
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
				return;
			}

			pm.report(token, "parameter.separator");
		}
	}

	public IParameter createParameter(IToken token)
	{
		final IParameter parameter = this.consumer
			                             .createParameter(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                              this.annotations);
		parameter.setVarargs(this.hasFlag(VARARGS));
		return parameter;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
