package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;

public final class ClassDeclarationParser extends Parser implements ITypeConsumer
{
	private static final int	MODIFIERS		= 0;
	private static final int	NAME			= 1;
	private static final int	GENERICS		= 2;
	private static final int	GENERICS_END	= 4;
	private static final int	PARAMETERS		= 8;
	private static final int	PARAMETERS_END	= 16;
	private static final int	EXTENDS			= 32;
	private static final int	IMPLEMENTS		= 64;
	private static final int	BODY			= 128;
	private static final int	BODY_END		= 256;
	
	private static final int	POST_EXTENDS	= IMPLEMENTS | BODY;
	private static final int	POST_PARAMETERS	= EXTENDS | POST_EXTENDS;
	private static final int	POST_GENERICS	= PARAMETERS | POST_PARAMETERS;
	private static final int	POST_NAME		= PARAMETERS | GENERICS | POST_PARAMETERS;
	
	protected IClassList	classList;
	private CodeClass		theClass;
	
	private int				modifiers;
	private IAnnotation[]	annotations;
	
	public ClassDeclarationParser(IDyvilHeader header)
	{
		this.classList = header;
		this.mode = MODIFIERS;
	}
	
	public ClassDeclarationParser(IClassList classList, CodeClass theClass)
	{
		this.classList = classList;
		this.theClass = theClass;
		
		if (theClass.getName() == null)
		{
			this.mode = NAME;
		}
		else
		{
			this.mode = POST_NAME;
		}
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (this.mode == MODIFIERS)
		{
			int i = 0;
			if ((i = ModifierTypes.CLASS.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if ((i = ModifierTypes.CLASS_TYPE.parse(type)) != -1)
			{
				this.modifiers |= i;
				this.mode = NAME;
				return;
			}
			if (type == Symbols.AT)
			{
				if (token.next().type() == Keywords.INTERFACE)
				{
					this.modifiers |= Modifiers.ANNOTATION;
					pm.skip();
					return;
				}
				
				Annotation annotation = new Annotation(token.raw());
				this.addAnnotation(annotation);
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			if (token.isInferred())
			{
				return;
			}
			pm.report(new SyntaxError(token, "Invalid " + token + " - Delete this token"));
			return;
		}
		if (this.mode == NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.theClass = new CodeClass(token.raw(), (IDyvilHeader) this.classList, this.modifiers);
				if (this.annotations != null)
				{
					this.theClass.setAnnotations(this.annotations, this.annotations.length);
				}
				this.theClass.setName(token.nameValue());
				this.mode = POST_NAME;
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Class Declaration - Name expected"));
			return;
		}
		if (this.isInMode(GENERICS))
		{
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeVariableListParser(this.theClass));
				this.theClass.setGeneric();
				this.mode = GENERICS_END;
				return;
			}
		}
		if (this.mode == GENERICS_END)
		{
			this.mode = POST_GENERICS;
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Generic Type Variable List - ']' expected"));
			return;
		}
		if (this.isInMode(PARAMETERS))
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.theClass));
				this.mode = PARAMETERS_END;
				return;
			}
		}
		if (this.mode == PARAMETERS_END)
		{
			this.mode = POST_PARAMETERS;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Class Parameter List - ')' expected"));
			return;
		}
		if (this.isInMode(EXTENDS))
		{
			if (type == Keywords.EXTENDS)
			{
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					pm.pushParser(new TypeListParser(this));
					this.mode = BODY;
					return;
				}
				
				pm.pushParser(pm.newTypeParser(this));
				this.mode = POST_EXTENDS;
				return;
			}
		}
		if (this.isInMode(IMPLEMENTS))
		{
			if (type == Keywords.IMPLEMENTS)
			{
				pm.pushParser(new TypeListParser(this));
				this.mode = BODY;
				
				if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					pm.report(new SyntaxError(token, "Interfaces cannot implement other interfaces - Use 'extends' instead"));
					return;
				}
				return;
			}
		}
		if (this.isInMode(BODY))
		{
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				IClassBody body = new ClassBody(this.theClass);
				this.theClass.setBody(body);
				pm.pushParser(new ClassBodyParser(this.theClass, body));
				this.mode = BODY_END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				if (token.isInferred())
				{
					int nextType = token.next().type();
					switch (nextType)
					{
					case Keywords.EXTENDS:
						this.mode = EXTENDS;
						return;
					case Keywords.IMPLEMENTS:
						this.mode = IMPLEMENTS;
						return;
					case Symbols.OPEN_SQUARE_BRACKET:
						this.mode = GENERICS;
						return;
					case Symbols.OPEN_PARENTHESIS:
						this.mode = PARAMETERS;
						return;
					}
				}
				
				pm.popParser();
				this.classList.addClass(this.theClass);
				return;
			}
			this.mode = BODY_END;
			pm.report(new SyntaxError(token, "Invalid Class Declaration - '{' or ';' expected"));
			return;
		}
		if (this.mode == BODY_END)
		{
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.classList.addClass(this.theClass);
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Class Declaration - '}' expected"));
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		switch (this.mode)
		{
		case IMPLEMENTS:
		case POST_EXTENDS: // extends
			this.theClass.setSuperType(type);
			return;
		case BODY: // implements
			this.theClass.addInterface(type);
			return;
		}
	}
	
	public void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new IAnnotation[1];
			this.annotations[0] = annotation;
			return;
		}
		
		int len = this.annotations.length;
		IAnnotation[] temp = new IAnnotation[len + 1];
		System.arraycopy(this.annotations, 0, temp, 0, this.annotations.length);
		temp[this.annotations.length] = annotation;
		this.annotations = temp;
	}
}
