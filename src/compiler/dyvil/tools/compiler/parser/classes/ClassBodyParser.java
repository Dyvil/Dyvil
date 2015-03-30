package dyvil.tools.compiler.parser.classes;

import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ExceptionListParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;

public final class ClassBodyParser extends Parser implements ITyped, IAnnotationList
{
	public static final int	TYPE			= 1;
	public static final int	NAME			= 2;
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
	
	private IBaseMethod		method;
	
	public ClassBodyParser(IClass theClass)
	{
		this.theClass = theClass;
		this.body = new ClassBody(theClass);
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
		
		if (type == Symbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.isInMode(TYPE))
		{
			if (type == Symbols.SEMICOLON)
			{
				if (token.isInferred())
				{
					return;
				}
				
				this.reset();
				return;
			}
			
			if (type == Keywords.NEW)
			{
				Constructor c = new Constructor(this.theClass);
				this.body.addConstructor(c);
				c.position = token.raw();
				c.modifiers = this.modifiers;
				c.setAnnotations(this.annotations, this.annotationCount);
				this.method = c;
				
				this.mode = PARAMETERS;
				return;
			}
			
			int i = 0;
			if ((i = ModifierTypes.MEMBER.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if ((i = ModifierTypes.CLASS_TYPE.parse(type)) != -1)
			{
				CodeClass codeClass = new CodeClass(null, this.theClass.getUnit(), this.modifiers);
				codeClass.setAnnotations(this.getAnnotations(), this.annotationCount);
				codeClass.setOuterClass(this.theClass);
				codeClass.setModifiers(this.modifiers);
				this.theClass.getBody().addClass(codeClass);
				
				ClassDeclarationParser parser = new ClassDeclarationParser(codeClass);
				pm.pushParser(parser, true);
				this.reset();
				return;
			}
			if (token.nameValue() == Name.at)
			{
				Annotation annotation = new Annotation(token.raw());
				this.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			
			pm.pushParser(new TypeParser(this), true);
			this.mode = NAME;
			return;
		}
		if (this.isInMode(NAME))
		{
			if (!ParserUtil.isIdentifier(type))
			{
				this.reset();
				throw new SyntaxError(token, "Invalid Member Declaration - Name expected");
			}
			
			IToken next = token.next();
			type = next.type();
			if (type == Symbols.SEMICOLON)
			{
				Field f = new Field(this.theClass, token.nameValue(), this.type);
				f.position = token.raw();
				f.modifiers = this.modifiers;
				f.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.body.addField(f);
				
				pm.skip();
				this.reset();
				return;
			}
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				this.mode = PARAMETERS;
				
				Method m = new Method(this.theClass, token.nameValue(), this.type);
				m.modifiers = this.modifiers;
				m.position = token.raw();
				m.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.method = m;
				this.body.addMethod(m);
				return;
			}
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				Property p = new Property(this.theClass, token.nameValue(), this.type);
				p.position = token.raw();
				p.modifiers = this.modifiers;
				p.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.body.addProperty(p);
				
				pm.skip();
				pm.pushParser(new PropertyParser(this.theClass, p));
				this.reset();
				return;
			}
			if (type == Symbols.EQUALS)
			{
				Field f = new Field(this.theClass, token.nameValue(), this.type);
				f.position = token.raw();
				f.modifiers = this.modifiers;
				f.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.body.addField(f);
				
				pm.skip();
				pm.pushParser(new ExpressionParser(f));
				this.reset();
				return;
			}
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				Method m = new Method(this.theClass, token.nameValue(), this.type);
				m.modifiers = this.modifiers;
				m.position = token.raw();
				m.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.method = m;
				this.body.addMethod(m);
				
				this.mode = GENERICS_END;
				pm.skip();
				pm.pushParser(new TypeVariableListParser(m));
				return;
			}
			return;
		}
		if (this.isInMode(GENERICS_END))
		{
			this.mode = PARAMETERS;
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Parameter List - ']' expected", true);
		}
		if (this.isInMode(PARAMETERS))
		{
			this.mode = PARAMETERS_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.method));
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - '(' expected", true);
		}
		if (this.isInMode(PARAMETERS_END))
		{
			this.mode = METHOD_END;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - ')' expected", true);
		}
		if (this.isInMode(METHOD_END))
		{
			if (type == Symbols.SEMICOLON)
			{
				this.reset();
				return;
			}
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new ExpressionParser(this.method), true);
				return;
			}
			if (type == Symbols.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.method));
				return;
			}
			if (type == Keywords.THROWS)
			{
				pm.pushParser(new ExceptionListParser(this.method));
				return;
			}
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
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
	public ElementType getAnnotationType()
	{
		return null;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
}
