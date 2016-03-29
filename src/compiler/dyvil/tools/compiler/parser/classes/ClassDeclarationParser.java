package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.consumer.IClassConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ArgumentListParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParameterListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ClassDeclarationParser extends Parser implements ITypeConsumer
{
	private static final int NAME                   = 1;
	private static final int GENERICS               = 2;
	private static final int GENERICS_END           = 4;
	private static final int PARAMETERS             = 8;
	private static final int PARAMETERS_END         = 16;
	private static final int EXTENDS                = 32;
	private static final int EXTENDS_PARAMETERS     = 64;
	private static final int EXTENDS_PARAMETERS_END = 128;
	private static final int IMPLEMENTS             = 256;
	private static final int BODY                   = 512;
	private static final int BODY_END               = 1024;

	protected IClassConsumer consumer;

	// Parsed and populated by the Unit / Header / Class Body parser; these values are just passed to the CodeClass constructors.
	protected ModifierSet    modifiers;
	protected AnnotationList annotations;
	
	private IClass theClass;
	
	public ClassDeclarationParser(IClassConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = NAME;
	}
	
	public ClassDeclarationParser(IClassConsumer consumer, ModifierSet modifiers, AnnotationList annotations)
	{
		this.consumer = consumer;
		
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (ParserUtil.isIdentifier(type))
			{
				this.theClass = this.consumer.createClass(token.raw(), token.nameValue(), this.modifiers, this.annotations);
				this.mode = GENERICS;
				return;
			}
			pm.report(token, "class.identifier");
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "class.generic.close_bracket");
			}
			return;
		case PARAMETERS_END:
			this.mode = EXTENDS;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "class.parameters.close_paren");
			}
			return;
		case GENERICS:
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeParameterListParser(this.theClass));
				this.theClass.setTypeParametric();
				this.mode = GENERICS_END;
				return;
			}
			// Fallthrough
		case PARAMETERS:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.theClass));
				this.mode = PARAMETERS_END;
				return;
			}
			// Fallthrough
		case EXTENDS:
			if (type == DyvilKeywords.EXTENDS)
			{
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
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
				
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
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
				IClassBody body = new ClassBody(this.theClass);
				this.theClass.setBody(body);
				pm.pushParser(new ClassBodyParser(body), true);
				this.mode = BODY_END;
				return;
			}
			if (ParserUtil.isTerminator(type))
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
			this.theClass.addInterface(type);
		}
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > NAME;
	}
}
