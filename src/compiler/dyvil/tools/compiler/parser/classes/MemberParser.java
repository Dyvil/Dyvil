package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.annotation.ModifierParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.header.DyvilHeaderParser;
import dyvil.tools.compiler.parser.header.PropertyParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParameterListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public final class MemberParser<T extends IDataMember> extends Parser implements ITypeConsumer
{
	protected static final int TYPE                       = 0;
	protected static final int NAME_OPERATOR              = 1;
	protected static final int NAME                       = 2;
	protected static final int FIELD_NAME                 = 4;
	protected static final int FIELD_TYPE                 = 5;
	protected static final int FIELD_SEPARATOR            = 6;
	protected static final int METHOD_NAME                = 7;
	protected static final int METHOD_SEPARATOR           = 8;
	protected static final int PARAMETERS                 = 9;
	protected static final int PARAMETERS_END             = 10;
	protected static final int GENERICS                   = 11;
	protected static final int GENERICS_END               = 12;
	protected static final int METHOD_TYPE                = 13;
	protected static final int METHOD_THROWS              = 14;
	protected static final int METHOD_VALUE               = 15;
	protected static final int CONSTRUCTOR_PARAMETERS     = 16;
	protected static final int CONSTRUCTOR_PARAMETERS_END = 17;
	protected static final int FIELD_PROPERTY             = 18;

	// Member Kinds

	// private static final byte IGNORE   = 0;
	private static final byte FIELD       = 1;
	private static final byte PROPERTY    = 2;
	private static final byte METHOD      = 3;
	private static final byte CONSTRUCTOR = 4;
	private static final byte INITIALIZER = 5;

	private static final byte MEMBER_KIND_MASK = 0x7;

	// Flags

	public static final int OPERATOR_ERROR             = 1 << 4;
	public static final int NO_UNINITIALIZED_VARIABLES = 1 << 5;
	public static final int NO_FIELD_PROPERTIES        = 1 << 6;

	// ----------

	protected IMemberConsumer<T> consumer;

	private IType type;
	private ModifierList modifiers = new ModifierList();
	private AnnotationList annotations;
	private ICodePosition  position;
	private Name           name;

	private IMember member;
	private int     flags;

	public MemberParser(IMemberConsumer<T> consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	public MemberParser<T> withFlags(int flag)
	{
		this.flags |= flag;
		return this;
	}

	private void setMemberKind(byte kind)
	{
		this.flags = this.flags & ~MEMBER_KIND_MASK | kind;
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
				// Fallthrough
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.reparse();
				// Fallthrough
			case Tokens.EOF:
				if (DyvilHeaderParser.hasModifiers(this.modifiers, this.annotations))
				{
					pm.report(token, "member.type");
				}

				pm.popParser();
				return;
			case DyvilKeywords.INIT: // constructor declaration or initializer
				if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET) // initializer
				{
					final IInitializer initializer = this.consumer.createInitializer(token.raw(), this.modifiers,
					                                                                 this.annotations);
					this.member = initializer;
					this.setMemberKind(INITIALIZER);

					this.mode = END;
					pm.pushParser(new StatementListParser(initializer));
					return;
				}

				this.member = this.consumer.createConstructor(token.raw(), this.modifiers, this.annotations);
				this.setMemberKind(CONSTRUCTOR);
				this.mode = CONSTRUCTOR_PARAMETERS;
				return;
			case DyvilKeywords.CONST:
				this.modifiers.addIntModifier(Modifiers.CONST);
				this.setMemberKind(FIELD);
				// Fallthrough
			case DyvilKeywords.LET:
				this.modifiers.addIntModifier(Modifiers.FINAL);
				this.setMemberKind(FIELD);
				// Fallthrough
			case DyvilKeywords.VAR:
				this.mode = FIELD_NAME;
				this.type = Types.UNKNOWN;
				return;
			case DyvilKeywords.FUNC:
			case DyvilKeywords.OPERATOR:
				this.mode = METHOD_NAME;
				this.type = Types.UNKNOWN;
				return;
			}

			final Modifier modifier;
			if ((modifier = ModifierParser.parseModifier(token, pm)) != null)
			{
				this.modifiers.addModifier(modifier);
				return;
			}

			int classType;
			if ((classType = ModifierParser.parseClassTypeModifier(token, pm)) >= 0)
			{
				this.modifiers.addIntModifier(classType);
				ClassDeclarationParser parser = new ClassDeclarationParser(this.consumer, this.modifiers,
				                                                           this.annotations);
				pm.pushParser(parser);
				this.mode = END;
				return;
			}

			// This is not in the above switch because 'readClassTypeModifier' above has to check for '@ interface' first
			if (type == DyvilSymbols.AT)
			{
				if (this.annotations == null)
				{
					this.annotations = new AnnotationList();
				}

				final Annotation annotation = new Annotation(token.raw());
				this.annotations.add(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}

			pm.pushParser(new TypeParser(this), true);
			this.mode = NAME_OPERATOR;
			return;
		case NAME_OPERATOR:
			if (type == DyvilKeywords.OPERATOR)
			{
				this.mode = NAME;
				return;
			}
			// Fallthrough
		case NAME:
			switch (type)
			{
			case Tokens.SYMBOL_IDENTIFIER:
			case Tokens.DOT_IDENTIFIER:
				if (token.prev().type() != DyvilKeywords.OPERATOR)
				{
					if ((this.flags & OPERATOR_ERROR) != 0)
					{
						// Produce an error instead of a warning
						pm.report(token, "member.symbol.operator");
						return;
					}
					pm.report(Markers.syntaxWarning(token, "member.symbol.operator"));
				}
				break;
			case Tokens.IDENTIFIER:
			case Tokens.LETTER_IDENTIFIER:
			case Tokens.SPECIAL_IDENTIFIER:
				if (token.prev().type() == DyvilKeywords.OPERATOR)
				{
					pm.report(Markers.syntaxWarning(token, "member.identifier.operator"));
				}
				break;
			default:
				if (this.parseSeparator(token))
				{
					// Improve error handling for missing identifiers
					this.position = token.raw();
					this.name = Names.apply;
					pm.reparse();
				}

				pm.report(token, "member.identifier");
				return;
			}

			this.name = token.nameValue();
			this.position = token.raw();

			if (!this.parseSeparator(token.next()))
			{
				pm.report(token, "class.body.declaration.invalid");
			}
			return;
		case FIELD_NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "field.identifier");
				return;
			}
			this.name = token.nameValue();
			this.position = token.raw();
			this.mode = FIELD_TYPE;
			return;
		case FIELD_TYPE:
			if (type == BaseSymbols.COLON)
			{
				if (this.type != Types.UNKNOWN)
				{
					pm.report(token, "field.type.duplicate");
				}

				pm.pushParser(new TypeParser(this.member != null ? this.member : this));
				this.mode = FIELD_SEPARATOR;
				return;
			}
			// Fallthrough
		case FIELD_SEPARATOR:
		{
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
			case Tokens.EOF:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
			{
				final T field = this.consumer.createDataMember(this.position, this.name, this.type, this.modifiers,
				                                               this.annotations);
				this.consumer.addDataMember(field);
				this.mode = END;
				pm.popParser(true);
				return;
			}
			case BaseSymbols.EQUALS:
			{
				final IDataMember field = this.consumer
					                          .createDataMember(this.position, this.name, this.type, this.modifiers,
					                                            this.annotations);
				this.member = field;
				this.setMemberKind(FIELD);

				if ((this.flags & NO_FIELD_PROPERTIES) != 0)
				{
					this.mode = END;
					pm.pushParser(new ExpressionParser(field));
				}
				else
				{
					this.mode = FIELD_PROPERTY;
					pm.pushParser(new ExpressionParser(field).withFlags(ExpressionParser.IGNORE_CLOSURE));
				}
				return;
			}
			case BaseSymbols.OPEN_CURLY_BRACKET:
			{
				if ((this.flags & MEMBER_KIND_MASK) == FIELD)
				{
					break;
				}

				final IProperty property = this.consumer
					                           .createProperty(this.position, this.name, this.type, this.modifiers,
					                                           this.annotations);
				this.member = property;
				this.setMemberKind(PROPERTY);
				this.mode = END;

				pm.pushParser(new PropertyParser(property));
				return;
			}
			}

			pm.report(token, "field.separator");
			return;
		}
		case METHOD_NAME:
		{
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "method.identifier");
				return;
			}

			final IMethod method = this.consumer.createMethod(token.raw(), token.nameValue(), this.type, this.modifiers,
			                                                  this.annotations);
			this.setMemberKind(METHOD);
			this.member = method;

			this.mode = GENERICS;
			return;
		}
		case METHOD_SEPARATOR:
		{
			final IMethod method = this.consumer.createMethod(this.position, this.name, this.type, this.modifiers,
			                                                  this.annotations);
			this.setMemberKind(METHOD);
			this.member = method;
		}
		// Fallthrough
		case GENERICS:
			if (TypeParser.isGenericStart(token, type))
			{
				pm.splitJump(token, 1);
				this.mode = GENERICS_END;
				pm.pushParser(new TypeParameterListParser((IMethod) this.member));
				return;
			}
			// Fallthrough
		case PARAMETERS:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = PARAMETERS_END;
				pm.pushParser(new ParameterListParser((IMethod) this.member));
				return;
			}
			// Fallthrough
		case METHOD_TYPE:
			switch (type)
			{
			case BaseSymbols.COLON:
				pm.report(Markers.syntaxWarning(token, "method.type.colon.deprecated"));
				// Fallthrough
			case DyvilSymbols.ARROW_RIGHT:
				if (this.type != Types.UNKNOWN)
				{
					pm.report(token, "method.type.duplicate");
				}

				pm.pushParser(new TypeParser(this.member));
				this.mode = METHOD_THROWS;
				return;
			}
			// Fallthrough
		case METHOD_THROWS:
			if (type == DyvilKeywords.THROWS)
			{
				pm.pushParser(new TypeListParser(((ICallableMember) this.member).getExceptions()));
				this.mode = METHOD_VALUE;
				return;
			}
			// Fallthrough
		case METHOD_VALUE:
			switch (type)
			{
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				this.consumer.addMethod((IMethod) this.member);
				pm.popParser(true);
				return;
			case BaseSymbols.SEMICOLON:
				this.consumer.addMethod((IMethod) this.member);
				pm.popParser(true);
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.pushParser(new StatementListParser((IValueConsumer) this.member), true);
				this.mode = END;
				return;
			case BaseSymbols.EQUALS:
				pm.pushParser(new ExpressionParser((IValueConsumer) this.member));
				this.mode = END;
				return;
			}

			pm.report(token, this.mode != METHOD_VALUE ? "method.declaration.separator" : "method.body.separator");
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (TypeParser.isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.reparse();
			pm.report(token, "generic.close_angle");
			return;
		case PARAMETERS_END:
			this.mode = METHOD_TYPE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "method.parameters.close_paren");
			}
			return;
		case CONSTRUCTOR_PARAMETERS:
			this.mode = CONSTRUCTOR_PARAMETERS_END;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser((IParametric) this.member));
				return;
			}
			pm.reparse();
			pm.report(token, "constructor.parameters.open_paren");
			return;
		case CONSTRUCTOR_PARAMETERS_END:
			this.mode = METHOD_VALUE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "constructor.parameters.close_paren");
			}
			return;
		case FIELD_PROPERTY:
			if (type == BaseSymbols.SEMICOLON && token.isInferred()
				    && token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				return;
			}
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				final IDataMember member = (IDataMember) this.member;
				final IProperty property = member.createProperty();
				pm.pushParser(new PropertyParser(property));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			switch (this.flags & MEMBER_KIND_MASK)
			{
			// case IGNORE: break;
			case METHOD:
				this.consumer.addMethod((IMethod) this.member);
				break;
			case CONSTRUCTOR:
				this.consumer.addConstructor((IConstructor) this.member);
				break;
			case INITIALIZER:
				this.consumer.addInitializer((IInitializer) this.member);
				break;
			case FIELD:
				this.consumer.addDataMember((T) this.member);
				break;
			case PROPERTY:
				this.consumer.addProperty((IProperty) this.member);
				break;
			}

			if (type == Tokens.EOF)
			{
				pm.popParser();
				return;
			}

			pm.popParser(true);
		}
	}

	private boolean parseSeparator(IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case Tokens.EOF:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			if ((this.flags & NO_UNINITIALIZED_VARIABLES) != 0)
			{
				// Produce an error
				return false;
			}
			// Fallthrough
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.EQUALS:
			this.mode = FIELD_TYPE;
			return true;
		case BaseSymbols.OPEN_PARENTHESIS:
			this.mode = METHOD_SEPARATOR;
			return true;
		}
		if (TypeParser.isGenericStart(token, type))
		{
			this.mode = METHOD_SEPARATOR;
			return true;
		}
		return false;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public boolean reportErrors()
	{
		return (this.mode > NAME || this.mode == END) && this.mode != CONSTRUCTOR_PARAMETERS;
	}
}
