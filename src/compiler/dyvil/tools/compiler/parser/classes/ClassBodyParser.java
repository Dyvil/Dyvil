package dyvil.tools.compiler.parser.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
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
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ClassBodyParser extends Parser implements ITyped, ITypeList, IAnnotated
{
	public static final int		TYPE			= 1;
	public static final int		NAME			= 2;
	public static final int		PROPERTY_END	= 8;
	public static final int		GENERICS_END	= 16;
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
	public void parse(ParserManager pm, IToken token) throws SyntaxError
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
			if ((i = Modifiers.MEMBER.parse(value)) != -1)
			{
				if ((this.modifiers & i) != 0)
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "' - Remove this Modifier");
				}
				this.modifiers |= i;
				return;
			}
			if ((i = Modifiers.CLASS_TYPE.parse(value)) != -1)
			{
				CodeClass codeClass = new CodeClass(null, this.theClass.getUnit(), this.modifiers, this.annotations);
				this.theClass.getBody().addClass(codeClass);
				codeClass.setOuterClass(this.theClass);
				
				ClassDeclParser parser = new ClassDeclParser(codeClass);
				pm.pushParser(parser);
				this.modifiers = 0;
				this.annotations = new ArrayList();
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
					Field f = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					f.setPosition(token.raw());
					this.body.addField(f);
					pm.skip();
					this.reset();
					return;
				}
				if (type == Tokens.OPEN_PARENTHESIS)
				{
					this.mode = PARAMETERS;
					this.method = new Method(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.method.setPosition(token.raw());
					this.body.addMethod(this.method);
					return;
				}
				if (type == Tokens.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY_END;
					Property property = new Property(this.theClass, value, this.type, this.modifiers, this.annotations);
					property.setPosition(token.raw());
					this.body.addProperty(property);
					pm.skip();
					pm.pushParser(new PropertyParser(this.theClass, property));
					return;
				}
				if (type == Tokens.EQUALS)
				{
					Field field = new Field(this.theClass, value, this.type, this.modifiers, this.annotations);
					field.setPosition(token.raw());
					this.body.addField(field);
					pm.skip();
					pm.pushParser(new ExpressionParser(field));
					this.reset();
					return;
				}
				if (type == Tokens.OPEN_SQUARE_BRACKET)
				{
					this.mode = GENERICS_END;
					this.method = new Method(this.theClass, value, this.type, this.modifiers, this.annotations);
					this.method.setPosition(token.raw());
					this.method.setGeneric();
					this.body.addMethod(this.method);
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
		this.method.addTypeVariable((ITypeVariable) type);
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
