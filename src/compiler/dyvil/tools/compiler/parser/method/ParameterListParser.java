package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IParameterConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
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

	protected IParameterConsumer consumer;
	protected boolean            untyped;

	// Metadata
	private ModifierList   modifiers;
	private AnnotationList annotations;

	private IType   type;
	private boolean varargs;

	public ParameterListParser(IParameterConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = TYPE;
	}

	public ParameterListParser(IParameterConsumer consumer, boolean untyped)
	{
		this.consumer = consumer;
		this.mode = TYPE;
		this.untyped = untyped;
	}

	private void reset()
	{
		this.modifiers = null;
		this.annotations = null;
		this.type = null;
		this.varargs = false;
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
			if ((modifier = BaseModifiers.parseModifier(token, pm)) != null)
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
				if (ParserUtil.isTerminator(nextType))
				{
					if (!this.untyped && nextType != BaseSymbols.COLON)
					{
						pm.report(token, "parameter.type");
					}

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
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = NAME;
			pm.pushParser(pm.newTypeParser(this), true);
			return;
		case NAME:
			switch (type)
			{
			case Tokens.EOF:
				pm.report(token, "parameter.identifier");
				pm.popParser();
				return;
			case DyvilSymbols.ELLIPSIS:
				if (this.varargs)
				{
					pm.report(token, "parameter.identifier");
					return;
				}
				this.varargs = true;
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
				pm.report(token, "parameter.identifier");
				return;
			}

			if (this.varargs)
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
				pm.pushParser(pm.newExpressionParser(parameter));
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
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}

			this.mode = TYPE;
			this.reset();
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.report(token, "parameter.comma");

				if (type == Tokens.EOF)
				{
					pm.popParser();
				}
			}
		}
	}

	public IParameter createParameter(IToken token)
	{
		final IParameter parameter = this.consumer
			                             .createParameter(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                              this.annotations);
		parameter.setVarargs(this.varargs);
		return parameter;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
