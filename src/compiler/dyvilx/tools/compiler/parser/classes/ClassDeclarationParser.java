package dyvilx.tools.compiler.parser.classes;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.Modifier;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.ExtensionMetadata;
import dyvilx.tools.compiler.ast.consumer.IClassConsumer;
import dyvilx.tools.compiler.ast.consumer.ITypeConsumer;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.compiler.parser.expression.ArgumentListParser;
import dyvilx.tools.compiler.parser.method.ParameterListParser;
import dyvilx.tools.compiler.parser.type.TypeListParser;
import dyvilx.tools.compiler.parser.type.TypeParameterListParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public final class ClassDeclarationParser extends Parser implements ITypeConsumer
{
	private static final int NAME                   = 0;
	private static final int GENERICS               = 1;
	private static final int GENERICS_END           = 2;
	private static final int PARAMETERS             = 3;
	private static final int PARAMETERS_END         = 4;
	private static final int EXTENDS                = 5;
	private static final int EXTENDS_PARAMETERS     = 6;
	private static final int EXTENDS_PARAMETERS_END = 7;
	private static final int IMPLEMENTS             = 8;
	private static final int BODY                   = 9;
	private static final int BODY_END               = 10;

	private static final int EXTENSION_GENERICS     = 11;
	private static final int EXTENSION_GENERICS_END = 12;
	private static final int EXTENSION_TYPE         = 13;

	protected IClassConsumer consumer;

	// Parsed and populated by the Unit / Header / Class Body parser; these values are just passed to the CodeClass constructors.
	protected AttributeList classAttributes;

	private IClass theClass;

	public ClassDeclarationParser(IClassConsumer consumer)
	{
		this.consumer = consumer;
		this.classAttributes = new AttributeList();
		// this.mode = NAME;
	}

	public ClassDeclarationParser(IClassConsumer consumer, AttributeList attributes)
	{
		this.consumer = consumer;
		this.classAttributes = attributes;
		// this.mode = NAME;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (this.classAttributes.hasFlag(Modifiers.EXTENSION_CLASS))
			{
				this.theClass = this.consumer.createClass(null, null, this.classAttributes);
				this.mode = EXTENSION_GENERICS;
				pm.reparse();
				return;
			}

			if (!Tokens.isIdentifier(type))
			{
				pm.report(token, "class.identifier");
				return;
			}

			final Name name = token.nameValue();
			if (name.qualified.indexOf('$') >= 0)
			{
				pm.report(Markers.syntaxError(token, "class.identifier.invalid", name, name.qualified));
			}

			this.theClass = this.consumer.createClass(token.raw(), name, this.classAttributes);
			this.mode = GENERICS;
			return;
		case GENERICS:
			if (type == BaseSymbols.SEMICOLON && token.isInferred() && TypeParser.isGenericStart(token.next()))
			{
				// allow an implicit semicolon / line break between name and generic argument list
				return;
			}
			if (TypeParser.isGenericStart(token, type))
			{
				pm.splitJump(token, 1);
				pm.pushParser(new TypeParameterListParser(this.theClass));
				this.mode = GENERICS_END;
				return;
			}
			// Fallthrough
		case PARAMETERS:
			final Modifier modifier = ModifierParser.parseModifier(token, pm);
			if (modifier != null)
			{
				this.theClass.getConstructorAttributes().add(modifier);
				return;
			}
			if (type == DyvilSymbols.AT)
			{
				final Annotation annotation = new CodeAnnotation(token.raw());
				this.theClass.getConstructorAttributes().add(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.theClass).withFlags(ParameterListParser.ALLOW_PROPERTIES));
				this.mode = PARAMETERS_END;
				return;
			}
			// Fallthrough
		case EXTENDS:
			if (type == DyvilKeywords.EXTENDS)
			{
				if (this.theClass.isInterface())
				{
					pm.pushParser(new TypeListParser(this));
					this.mode = BODY;
					return;
				}

				pm.pushParser(new TypeParser(this));
				this.mode = EXTENDS_PARAMETERS;
				return;
			}
			// Fallthrough
		case IMPLEMENTS:
			if (type == DyvilKeywords.IMPLEMENTS)
			{
				pm.pushParser(new TypeListParser(this));
				this.mode = BODY;

				if (this.theClass.isInterface())
				{
					pm.report(token, "class.interface.implements");
					return;
				}
				return;
			}
			// Fallthrough
		case BODY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				ClassBody body = new ClassBody(this.theClass);
				this.theClass.setBody(body);
				pm.pushParser(new ClassBodyParser(body), true);
				this.mode = BODY_END;
				return;
			}
			if (BaseSymbols.isTerminator(type))
			{
				if (token.isInferred())
				{
					switch (token.next().type())
					{
					case DyvilKeywords.EXTENDS:
						this.mode = EXTENDS;
						return;
					case DyvilKeywords.IMPLEMENTS:
						this.mode = IMPLEMENTS;
						return;
					case BaseSymbols.OPEN_SQUARE_BRACKET:
						this.mode = GENERICS;
						return;
					case BaseSymbols.OPEN_PARENTHESIS:
						this.mode = PARAMETERS;
						return;
					}
				}

				pm.popParser(true);
				this.consumer.addClass(this.theClass);
				return;
			}

			this.mode = BODY_END;
			pm.report(token, "class.body.separator");
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
			this.mode = EXTENDS;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "class.parameters.close_paren");
			}
			return;
		case BODY_END:
			pm.popParser();
			this.consumer.addClass(this.theClass);
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "class.body.close_brace");
			}
			return;
		case EXTENDS_PARAMETERS_END:
			this.mode = IMPLEMENTS;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "class.extends.close_paren");
			}
			return;
		case EXTENDS_PARAMETERS:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				ArgumentListParser.parseArguments(pm, token.next(), this.theClass::setSuperConstructorArguments);
				this.mode = EXTENDS_PARAMETERS_END;
				return;
			}
			this.mode = IMPLEMENTS;
			pm.reparse();
			return;
		case EXTENSION_GENERICS:
			assert this.classAttributes.hasFlag(Modifiers.EXTENSION_CLASS);

			if (TypeParser.isGenericStart(token, type))
			{
				// extension < ...

				ExtensionMetadata metadata = (ExtensionMetadata) this.theClass.getMetadata();

				pm.splitJump(token, 1);
				pm.pushParser(new TypeParameterListParser(metadata));
				this.mode = EXTENSION_GENERICS_END;
				return;
			}
			// Fallthrough
		case EXTENSION_TYPE:
			assert this.classAttributes.hasFlag(Modifiers.EXTENSION_CLASS);

			ExtensionMetadata metadata = (ExtensionMetadata) this.theClass.getMetadata();
			pm.pushParser(new TypeParser(metadata::setExtendedType), true);
			this.mode = BODY;
			return;
		case EXTENSION_GENERICS_END:
			// extension < ... >

			this.mode = EXTENSION_TYPE;
			if (TypeParser.isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.reparse();
			pm.report(token, "generic.close_angle");
			return;
		}
	}

	@Override
	public void setType(IType type)
	{
		switch (this.mode)
		{
		case EXTENDS:
		case IMPLEMENTS:
		case EXTENDS_PARAMETERS: // extends
			this.theClass.setSuperType(type);
			return;
		case BODY: // implements
			this.theClass.getInterfaces().add(type);
		}
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > NAME;
	}
}
