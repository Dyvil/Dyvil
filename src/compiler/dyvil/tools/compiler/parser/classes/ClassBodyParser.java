package dyvil.tools.compiler.parser.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.IAnnotated;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.method.ThrowsDeclParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Tokens;

public class ClassBodyParser extends Parser implements ITyped, ITypeList, IAnnotated
{
	public static final int		TYPE			= 1;
	public static final int		NAME			= 2;
	public static final int		FIELD			= 4;
	public static final int		PROPERTY		= 8;
	public static final int		GENERICS		= 16;
	public static final int		PARAMETERS		= 32;
	public static final int		PARAMETERS_END	= 64;
	public static final int		METHOD_END		= 128;
	public static final int		BODY_END		= 256;
	
	public static final int		DEFAULT_MODE	= TYPE | BODY_END;
	
	protected IClass			theClass;
	protected ClassBody			body;
	
	private IType				type;
	private int					modifiers;
	private List<Annotation>	annotations;
	
	private IMethod				method;
	private IField				field;
	
	public ClassBodyParser(IClass theClass)
	{
		this.theClass = theClass;
		this.body = new ClassBody(null, theClass);
		theClass.setBody(this.body);
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
			if (token.isType(Tokens.OPEN_CURLY_BRACKET))
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
				pm.pushParser(new AnnotationParser(this), true);
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
			if (token.isType(Tokens.TYPE_IDENTIFIER))
			{
				IToken next = token.next();
				int type = next.type();
				if (next.equals("="))
				{
					this.mode = FIELD;
					this.field = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.field.setPosition(token.raw());
					this.body.addField(this.field);
					return true;
				}
				else if (type == Tokens.SEMICOLON)
				{
					Field f = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					f.setPosition(token.raw());
					this.body.addField(f);
					pm.skip();
					this.reset();
					return true;
				}
				else if (type == Tokens.OPEN_PARENTHESIS)
				{
					this.mode = PARAMETERS;
					this.method = new Method(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.method.setPosition(token.raw());
					this.body.addMethod(this.method);
					return true;
				}
				else if (type == Tokens.OPEN_CURLY_BRACKET)
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
				else if (next.equals("<"))
				{
					this.mode = GENERICS;
					this.method = new Method(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.method.setPosition(token.raw());
					this.method.setGeneric();
					this.body.addMethod(this.method);
					pm.skip();
					pm.pushParser(new TypeListParser(this, true));
					return true;
				}
			}
			return false;
		}
		if (this.isInMode(FIELD))
		{
			if ("=".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.field));
				return true;
			}
			else if (token.isType(Tokens.SEMICOLON))
			{
				this.reset();
				return true;
			}
			return false;
		}
		if (this.isInMode(PROPERTY))
		{
			if (token.isType(Tokens.CLOSE_CURLY_BRACKET))
			{
				this.reset();
				return true;
			}
			return false;
		}
		if (this.isInMode(GENERICS))
		{
			if (">".equals(value))
			{
				this.mode = PARAMETERS;
				return true;
			}
			return false;
		}
		if (this.isInMode(PARAMETERS))
		{
			if (token.isType(Tokens.OPEN_PARENTHESIS))
			{
				pm.pushParser(new ParameterListParser(this.method));
				this.mode = PARAMETERS_END;
				return true;
			}
			return false;
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (token.isType(Tokens.CLOSE_PARENTHESIS))
			{
				this.mode = METHOD_END;
				return true;
			}
			return false;
		}
		if (this.isInMode(METHOD_END))
		{
			if ("=".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.method));
				return true;
			}
			if (token.isType(Tokens.SEMICOLON))
			{
				this.reset();
				return true;
			}
			if ("throws".equals(value))
			{
				pm.pushParser(new ThrowsDeclParser(this.method));
				return true;
			}
			// "Insert" a semicolon after a closing curly bracket.
			if (token.prev().isType(Tokens.CLOSE_CURLY_BRACKET))
			{
				this.mode = DEFAULT_MODE;
				pm.reparse();
				return true;
			}
			return false;
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
	public void setTypes(List<IType> types)
	{
	}
	
	@Override
	public List<IType> getTypes()
	{
		return null;
	}
	
	@Override
	public void addType(IType type)
	{
		this.method.addType(type);
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
