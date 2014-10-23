package dyvil.tools.compiler.parser.classes;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.method.ThrowsDeclParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;

public class ClassBodyParser extends Parser implements ITyped, IAnnotatable
{
	public static int	TYPE			= 1;
	public static int	NAME			= 2;
	public static int	FIELD			= 4;
	public static int	METHOD			= 8;
	public static int	METHOD_END		= 16;
	public static int	ANNOTATION		= 32;
	public static int	ANNOTATION_END	= 128;
	
	protected IClass	theClass;
	protected ClassBody	classBody;
	
	private IField		field;
	private IMethod		method;
	
	public ClassBodyParser(IClass theClass)
	{
		this.theClass = theClass;
		this.reset();
	}
	
	private void reset()
	{
		this.mode = TYPE | ANNOTATION;
		this.field = new Field(this.theClass);
		this.method = new Method(this.theClass);
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		int i = 0;
		if (this.classBody == null)
		{
			this.classBody = new ClassBody(token);
			this.classBody.setTheClass(this.theClass);
			this.theClass.setBody(this.classBody);
		}
		
		if ("}".equals(value))
		{
			pm.popParser(token);
			this.classBody.expandPosition(token.prev());
			return true;
		}
		if (this.isInMode(TYPE))
		{
			if ((i = Modifiers.FIELD_OR_METHOD.parse(value)) != -1)
			{
				this.field.addModifier(i);
				this.method.addModifier(i);
				return true;
			}
			else if (value.indexOf('@') == 0)
			{
				pm.pushParser(new AnnotationParser(this.theClass, this), token);
				return true;
			}
			else
			{
				pm.pushParser(new TypeParser(this), token);
				this.mode = NAME;
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(Token.TYPE_IDENTIFIER))
			{
				IToken next = token.next();
				if (next.equals("="))
				{
					this.mode = FIELD;
					this.field.setName(value);
					this.classBody.addVariable(field);
					return true;
				}
				else if (next.equals(";"))
				{
					this.mode = FIELD;
					this.field.setName(value);
					this.classBody.addVariable(this.field);
					this.reset();
					return true;
				}
				else if (next.isType(Token.TYPE_BRACKET))
				{
					this.mode = METHOD;
					this.method.setName(value);
					this.classBody.addMethod(this.method);
					return true;
				}
			}
			else if (";".equals(value))
			{
				this.reset();
				return true;
			}
			
			pm.pushParser(new TypeParser(this), token);
			return true;
		}
		if (this.isInMode(FIELD))
		{
			if ("=".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.theClass, this.field));
				return true;
			}
			else if (";".equals(value))
			{
				this.reset();
				return true;
			}
		}
		if (this.isInMode(METHOD))
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ParameterListParser(this.method));
				return true;
			}
			else if (")".equals(value))
			{
				this.mode = METHOD_END;
				return true;
			}
		}
		if (this.isInMode(METHOD_END))
		{
			if ("throws".equals(value))
			{
				pm.pushParser(new ThrowsDeclParser(this.method));
				return true;
			}
			else if ("=".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.method, this.method, true));
				return true;
			}
			else if (";".equals(value))
			{
				this.reset();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setType(Type type)
	{
		this.field.setType(type);
		this.method.setType(type);
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return null;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.field.addAnnotation(annotation);
		this.method.addAnnotation(annotation);
	}
}
