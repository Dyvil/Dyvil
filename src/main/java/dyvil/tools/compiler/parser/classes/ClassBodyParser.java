package dyvil.tools.compiler.parser.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
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
	public static final int		TYPE			= 1;
	public static final int		NAME			= 2;
	public static final int		FIELD			= 4;
	public static final int		PROPERTY		= 8;
	public static final int		PARAMETERS		= 16;
	public static final int		PARAMETERS_END	= 32;
	public static final int		METHOD_END		= 64;
	public static final int		BODY_END		= 128;
	
	public static final int		DEFAULT_MODE	= TYPE | BODY_END;
	
	protected IClass			theClass;
	protected ClassBody			body;
	
	private IType				type;
	private int					modifiers;
	private List<Annotation>	annotations;
	
	private IMethod				method;
	private IField				field;
	
	public ClassBodyParser(IClass theClass, ClassBody body)
	{
		this.theClass = theClass;
		this.body = body;
		this.reset();
	}
	
	private void reset()
	{
		this.mode = DEFAULT_MODE;
		this.modifiers = 0;
		this.annotations = new ArrayList();
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(BODY_END))
		{
			if ("}".equals(value))
			{
				pm.popParser(true);
				return true;
			}
		}
		if (this.isInMode(TYPE))
		{
			int i = 0;
			if ((i = Modifiers.FIELD_OR_METHOD.parse(value)) != -1)
			{
				if ((this.modifiers & i) != 0)
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "'", "Remove this Modifier");
				}
				this.modifiers |= i;
				return true;
			}
			else if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this.theClass, this), true);
				return true;
			}
			else
			{
				pm.pushParser(new TypeParser(this), true);
				this.mode = NAME;
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				IToken next = token.next();
				if (next.equals("="))
				{
					this.mode = FIELD;
					this.field = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.field.setPosition(token.raw());
					this.body.addField(this.field);
					return true;
				}
				else if (next.equals(";"))
				{
					Field f = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					f.setPosition(token.raw());
					this.body.addField(f);
					pm.skip();
					this.reset();
					return true;
				}
				else if (next.equals("("))
				{
					this.mode = PARAMETERS;
					this.method = new Method(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.method.setPosition(token.raw());
					this.body.addMethod(this.method);
					return true;
				}
				else if (next.equals("{"))
				{
					this.mode = PROPERTY;
					Property property = new Property(this.theClass, value, this.type, this.modifiers, this.annotations);
					property.setPosition(token.raw());
					this.body.addProperty(property);
					this.field = property;
					pm.skip();
					pm.pushParser(new PropertyParser(this.theClass, property));
					return true;
				}
			}
			else if (";".equals(value))
			{
				this.reset();
				return true;
			}
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
		if (this.isInMode(PROPERTY))
		{
			if ("}".equals(value))
			{
				this.reset();
				return true;
			}
		}
		if (this.isInMode(PARAMETERS))
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ParameterListParser(this.theClass, this.method));
				this.mode = PARAMETERS_END;
				return true;
			}
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (")".equals(value))
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
				pm.pushParser(new ExpressionParser(this.method, this.method));
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
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.annotations.add(annotation);
	}
}
