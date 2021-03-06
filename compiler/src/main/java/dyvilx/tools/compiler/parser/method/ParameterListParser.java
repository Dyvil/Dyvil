package dyvilx.tools.compiler.parser.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.Modifier;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.IParametric;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.compiler.parser.classes.PropertyBodyParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public final class ParameterListParser extends Parser implements Consumer<IType>
{
	public static final int DECLARATOR              = 0;
	public static final int NAME                    = 1;
	public static final int INTERNAL_NAME           = 2;
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
	private AttributeList attributes = new AttributeList();

	private IType type = Types.UNKNOWN;

	private IParameter parameter;

	private byte flags;

	public ParameterListParser(IParametric consumer)
	{
		this.consumer = consumer;
		// this.mode = DECLARATOR;
	}

	public ParameterListParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	private void reset()
	{
		this.attributes = new AttributeList();
		this.type = Types.UNKNOWN;
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
		case DECLARATOR:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				break;
			case DyvilKeywords.LET:
				this.attributes.addFlag(Modifiers.FINAL);
				// Fallthrough
			case DyvilKeywords.VAR:
				this.mode = NAME;
				return;
			case DyvilKeywords.THIS:
				if (token.next().type() != BaseSymbols.COLON)
				{
					pm.report(token, "parameter.identifier");
					this.mode = SEPARATOR;
					return;
				}

				// this : TYPE
				this.mode = TYPE_ASCRIPTION;
				pm.skip(); // the colon
				pm.pushParser(new TypeParser(t -> this.setThisType(t, token, pm)));
				return;
			case DyvilSymbols.AT:
				final Annotation annotation = new CodeAnnotation(token.raw());
				this.attributes.add(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}

			final Modifier modifier;
			if ((modifier = ModifierParser.parseModifier(token, pm)) != null)
			{
				this.attributes.add(modifier);
				return;
			}

			if (BaseSymbols.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			// Fallthrough
		case NAME:
			final Name name;
			if (Tokens.isIdentifier(type))
			{
				name = token.nameValue();
			}
			else if (type == BaseSymbols.UNDERSCORE)
			{
				name = null;
			}
			else if (Tokens.isKeyword(type))
			{
				name = Name.fromRaw(token.stringValue());
			}
			else
			{
				if (BaseSymbols.isCloseBracket(type))
				{
					pm.popParser(true);
				}
				if (type == Tokens.EOF)
				{
					pm.popParser();
				}

				this.mode = SEPARATOR;
				pm.report(token, "parameter.identifier");
				return;
			}

			this.parameter = this.consumer.createParameter(token.raw(), name, this.type, this.attributes);
			this.mode = INTERNAL_NAME;
			return;
		case INTERNAL_NAME:
			this.mode = VARARGS_AFTER_NAME;
			// overwrite the internal name if necessary
			if (Tokens.isIdentifier(type))
			{
				// IDENTIFIER IDENTIFIER : TYPE
				this.parameter.setName(token.nameValue());
				return;
			}
			else if (type == BaseSymbols.UNDERSCORE)
			{
				// IDENTIFIER _ : TYPE
				this.parameter.setName(null);
				return;
			}
			// Fallthrough
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
				this.mode = VARARGS_AFTER_POST_TYPE;
				final TypeParser parser = new TypeParser(this);
				if (this.hasFlag(LAMBDA_ARROW_END))
				{
					parser.withFlags(TypeParser.IGNORE_LAMBDA);
				}
				pm.pushParser(parser);
				return;
			}
			// Fallthrough
		case DEFAULT_VALUE:
			if (type == BaseSymbols.EQUALS)
			{
				this.mode = PROPERTY;
				pm.pushParser(new ExpressionParser(this.parameter::setValue));
				return;
			}
			// Fallthrough
		case PROPERTY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET && this.hasFlag(ALLOW_PROPERTIES))
			{
				final IProperty property = this.parameter.createProperty();
				pm.pushParser(new PropertyBodyParser(property), true);
				this.mode = SEPARATOR;
				return;
			}
			// Fallthrough
		case SEPARATOR:
			this.mode = DECLARATOR;
			if (this.parameter != null)
			{
				if (this.hasFlag(VARARGS))
				{
					this.parameter.setVarargs();
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

	private void setThisType(IType type, IToken token, IParserManager pm)
	{
		if (!this.consumer.setThisType(type))
		{
			pm.report(token, "parameter.this_type.invalid");
		}
	}

	private boolean isTerminator(int nextType)
	{
		return BaseSymbols.isTerminator(nextType)
		       || (nextType == DyvilSymbols.ARROW_RIGHT || nextType == DyvilSymbols.DOUBLE_ARROW_RIGHT) && this.hasFlag(
			LAMBDA_ARROW_END);
	}

	protected void setTypeVarargs()
	{
		// Ellipsis after the type automatically converts it to an array type (see #333)
		this.type = new ArrayType(this.type);
		this.flags |= VARARGS;
	}

	@Override
	public void accept(IType type)
	{
		this.type = type;
	}
}
