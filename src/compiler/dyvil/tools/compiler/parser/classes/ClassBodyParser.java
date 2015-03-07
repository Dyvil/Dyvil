package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.method.ThrowsDeclParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public final class ClassBodyParser extends Parser implements ITyped, ITypeList, IAnnotationList
{
	public static final int	TYPE			= 1;
	public static final int	NAME			= 2;
	public static final int	PROPERTY_END	= 8;
	public static final int	GENERICS_END	= 16;
	public static final int	PARAMETERS		= 32;
	public static final int	PARAMETERS_END	= 64;
	public static final int	METHOD_END		= 128;
	public static final int	BODY_END		= 256;
	
	public static final int	DEFAULT_MODE	= TYPE | BODY_END;
	
	protected IClass		theClass;
	protected ClassBody		body;
	
	private IType			type;
	private int				modifiers;
	private Annotation[]	annotations		= new Annotation[2];
	private int				annotationCount;
	
	private IMethod			method;
	
	public ClassBodyParser(IClass theClass)
	{
		this.theClass = theClass;
		this.body = new ClassBody(null, theClass);
		theClass.setBody(this.body);
		this.mode = DEFAULT_MODE;
	}
	
	@Override
	public void reset()
	{
		this.mode = DEFAULT_MODE;
		this.modifiers = 0;
		this.annotationCount = 0;
		this.type = null;
		this.method = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			this.reset();
			return;
		}
		
		String value = token.value();
		if (this.isInMode(BODY_END))
		{
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				pm.popParser(true);
				return;
			}
		}
		if (this.isInMode(TYPE))
		{
			int i = 0;
			if ((i = ModifierTypes.MEMBER.parse(value)) != -1)
			{
				if ((this.modifiers & i) != 0)
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "' - Remove this Modifier");
				}
				this.modifiers |= i;
				return;
			}
			if ((i = ModifierTypes.CLASS_TYPE.parse(value)) != -1)
			{
				CodeClass codeClass = new CodeClass(null, this.theClass.getUnit(), this.modifiers);
				codeClass.setAnnotations(this.getAnnotations(), this.annotationCount);
				codeClass.setOuterClass(this.theClass);
				this.theClass.getBody().addClass(codeClass);
				
				ClassDeclParser parser = new ClassDeclParser(codeClass);
				pm.pushParser(parser);
				this.modifiers = 0;
				return;
			}
			if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this), true);
				return;
			}
			
			pm.pushParser(new TypeParser(this), true);
			this.mode = NAME;
			return;
		}
		if (this.isInMode(NAME))
		{
			if (ParserUtil.isIdentifier(type) || type == Tokens.NEW)
			{
				IToken next = token.next();
				type = next.type();
				if (type == Tokens.SEMICOLON)
				{
					Field f = new Field(this.theClass, value, this.type);
					f.position = token.raw();
					f.modifiers = this.modifiers;
					f.setAnnotations(this.getAnnotations(), this.annotationCount);
					this.body.addField(f);
					
					pm.skip();
					this.reset();
					return;
				}
				if (type == Tokens.OPEN_PARENTHESIS)
				{
					this.mode = PARAMETERS;
					
					Method m = new Method(this.theClass, value, this.type);
					m.modifiers = this.modifiers;
					m.position = token.raw();
					m.setAnnotations(this.getAnnotations(), this.annotationCount);
					this.method = m;
					this.body.addMethod(this.method);
					return;
				}
				if (type == Tokens.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY_END;
					Property p = new Property(this.theClass, value, this.type);
					p.position = token.raw();
					p.modifiers = this.modifiers;
					p.setAnnotations(this.getAnnotations(), this.annotationCount);
					this.body.addProperty(p);
					
					pm.skip();
					pm.pushParser(new PropertyParser(this.theClass, p));
					return;
				}
				if (type == Tokens.EQUALS)
				{
					Field f = new Field(this.theClass, value, this.type);
					f.position = token.raw();
					f.modifiers = this.modifiers;
					f.setAnnotations(this.getAnnotations(), this.annotationCount);
					this.body.addField(f);
					
					pm.skip();
					pm.pushParser(new ExpressionParser(f));
					this.reset();
					return;
				}
				if (type == Tokens.OPEN_SQUARE_BRACKET)
				{
					Method m = new Method(this.theClass, value, this.type);
					m.modifiers = this.modifiers;
					m.position = token.raw();
					m.setAnnotations(this.getAnnotations(), this.annotationCount);
					this.method = m;
					this.body.addMethod(this.method);
					
					this.mode = GENERICS_END;
					pm.skip();
					pm.pushParser(new TypeVariableListParser(this.method));
					return;
				}
			}
			this.reset();
			throw new SyntaxError(token, "Invalid Member Declaration - Name expected");
		}
		if (this.isInMode(PROPERTY_END))
		{
			this.reset();
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Property Declaration - '}' expected", true);
		}
		if (this.isInMode(GENERICS_END))
		{
			this.mode = PARAMETERS;
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Parameter List - ']' expected", true);
		}
		if (this.isInMode(PARAMETERS))
		{
			this.mode = PARAMETERS_END;
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.method));
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - '(' expected", true);
		}
		if (this.isInMode(PARAMETERS_END))
		{
			this.mode = METHOD_END;
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - ')' expected", true);
		}
		if (this.isInMode(METHOD_END))
		{
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new ExpressionParser(this.method), true);
				return;
			}
			if (type == Tokens.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.method));
				return;
			}
			if ("throws".equals(value))
			{
				pm.pushParser(new ThrowsDeclParser(this.method));
				return;
			}
		}
		
		// "Insert" a semicolon after a closing curly bracket.
		if (token.prev().type() == Tokens.CLOSE_CURLY_BRACKET)
		{
			this.reset();
			pm.reparse();
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public void addType(IType type)
	{
		this.method.addTypeVariable((ITypeVariable) type);
	}
	
	private Annotation[] getAnnotations()
	{
		Annotation[] a = new Annotation[this.annotationCount];
		System.arraycopy(this.annotations, 0, a, 0, this.annotationCount);
		return a;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			Annotation[] temp = new Annotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
	
	// Override Methods
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
	}
	
	@Override
	public void removeAnnotation(int index)
	{
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public int typeCount()
	{
		return 0;
	}
	
	@Override
	public void setType(int index, IType type)
	{
	}
	
	@Override
	public IType getType(int index)
	{
		return null;
	}
}
