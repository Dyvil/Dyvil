package dyvil.tools.compiler.parser.method;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.annotation.ModifierParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.header.PropertyParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int TYPE                    = 0;
	public static final int VARARGS_AFTER_PRE_TYPE  = 1;
	public static final int NAME                    = 2;
	public static final int VARARGS_AFTER_NAME      = 3;
	public static final int TYPE_ASCRIPTION         = 4;
	public static final int VARARGS_AFTER_POST_TYPE = 5;
	public static final int DEFAULT_VALUE           = 6;
	public static final int PROPERTY                = 7;
	public static final int SEPARATOR               = 8;

	// Flags

	public static final byte VARARGS          = 1;
	public static final byte LAMBDA_ARROW_END = 2;
	public static final byte ALLOW_PROPERTIES = 4;

	protected IParametric consumer;

	// Metadata
	private ModifierList   modifiers;
	private AnnotationList annotations;

	private IType      type;
	private IParameter parameter;

	private byte flags;

	public ParameterListParser(IParametric consumer)
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
		this.parameter = null;
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
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				break;
			case DyvilKeywords.LET:
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}
				this.modifiers.addIntModifier(Modifiers.FINAL);
				// Fallthrough
			case DyvilKeywords.VAR:
				this.mode = NAME;
				this.type = Types.UNKNOWN;
				return;
			case DyvilSymbols.AT:
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}

				final Annotation annotation = new Annotation(token.raw());
				this.annotations.add(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}

			final Modifier modifier;
			if ((modifier = ModifierParser.parseModifier(token, pm)) != null)
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
				if (this.canAppearAfterName(nextType) || nextType == DyvilSymbols.ELLIPSIS && this.canAppearAfterName(
					token.next().next().type()))
				{
					// ... , IDENTIFIER (...) , ...
					// ... , IDENTIFIER (...) =>
					// ... , IDENTIFIER (...) ->
					this.type = Types.UNKNOWN;
					this.mode = NAME;
					pm.reparse();
					return;
				}
			}

			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = VARARGS_AFTER_PRE_TYPE;
			pm.pushParser(new TypeParser(this), true);
			return;
		case VARARGS_AFTER_PRE_TYPE:
			if (type == DyvilSymbols.ELLIPSIS)
			{
				// TYPE ...
				this.setTypeVarargs();
				this.mode = NAME;
				return;
			}
			// Fallthrough
		case NAME:
			switch (type)
			{
			case Tokens.EOF:
				pm.report(token, "parameter.identifier");
				pm.popParser();
				return;
			case DyvilKeywords.THIS:
				this.mode = SEPARATOR;
				if (!this.consumer.setThisType(this.type))
				{
					pm.report(token, "parameter.receivertype.invalid");
				}
				this.reset();
				return;
			}

			if (!ParserUtil.isIdentifier(type))
			{
				if (ParserUtil.isCloseBracket(type))
				{
					pm.popParser(true);
				}

				this.mode = SEPARATOR;
				pm.report(token, "parameter.identifier");
				return;
			}

			this.parameter = this.consumer.createParameter(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                               this.annotations);
			this.mode = VARARGS_AFTER_NAME;
			return;
		case VARARGS_AFTER_NAME:
			if (type == DyvilSymbols.ELLIPSIS)
			{
				this.flags |= VARARGS;
				this.mode = TYPE_ASCRIPTION;
				return;
			}
			// Fallthrough
		case TYPE_ASCRIPTION:
		case VARARGS_AFTER_POST_TYPE:
			// The following code has a bit of an unusual structure. That is because there are two ways to come here:
			// (1) after an identifier (NAME), optionally followed by an ellipsis (this.mode != VARARGS_AFTER_POST_TYPE)
			// (2) after a type (this.mode == VARARGS_AFTER_POST_TYPE)
			// In case (1), we do not expect another ellipsis. Thus, we only check for a colon that indicates a type
			// ascription, or continue with the other cases (DEFAULT_VALUE, PROPERTY, ...).
			// In case (2), we do not expect another colon, but allow an optional ellipsis. If there is none, we
			// continue with the remaining cases (DEFAULT_VALUE, PROPERTY, ...).
			if (this.mode == VARARGS_AFTER_POST_TYPE)
			{
				// case (2)
				if (type == DyvilSymbols.ELLIPSIS)
				{
					this.setTypeVarargs();
					this.mode = DEFAULT_VALUE;
					return;
				}
			}
			else /* case (1) */ if (type == BaseSymbols.COLON)
			{
				if (this.type != Types.UNKNOWN)
				{
					pm.report(token, "parameter.type.duplicate");
				}

				this.mode = VARARGS_AFTER_POST_TYPE;
				pm.pushParser(new TypeParser(this));
				return;
			}
			// Fallthrough
		case DEFAULT_VALUE:
			if (type == BaseSymbols.EQUALS)
			{
				this.mode = PROPERTY;
				pm.pushParser(new ExpressionParser(this.parameter));
				return;
			}
			// Fallthrough
		case PROPERTY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET && this.hasFlag(ALLOW_PROPERTIES))
			{
				final IProperty property = this.parameter.createProperty();
				pm.pushParser(new PropertyParser(property));
				this.mode = SEPARATOR;
				return;
			}
			// Fallthrough
		case SEPARATOR:
			this.mode = TYPE;
			if (this.parameter != null)
			{
				if (this.hasFlag(VARARGS))
				{
					this.parameter.setVarargs(true);
				}
				this.parameter.setType(this.type);
				this.consumer.getParameters().add(this.parameter);
			}
			this.reset();

			switch (type)
			{
			case DyvilSymbols.ARROW_RIGHT:
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

	private boolean canAppearAfterName(int nextType)
	{
		return ParserUtil.isTerminator(nextType)
			       || (nextType == DyvilSymbols.ARROW_RIGHT || nextType == DyvilSymbols.DOUBLE_ARROW_RIGHT)
				          && this.hasFlag(LAMBDA_ARROW_END);
	}

	protected void setTypeVarargs()
	{
		// Ellipsis after the type automatically converts it to an array type (see #333)
		this.type = new ArrayType(this.type);
		this.flags |= VARARGS;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
