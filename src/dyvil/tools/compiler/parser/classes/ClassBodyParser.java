package dyvil.tools.compiler.parser.classes;

import java.lang.reflect.Constructor;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.codeblock.CodeBlockParser;
import dyvil.tools.compiler.parser.field.ValueParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.method.ThrowsDeclParser;

public class ClassBodyParser extends Parser
{
	public static int		VARIABLE	= 1;
	public static int		METHOD		= 2;
	public static int		CONSTRUCTOR	= 3;
	public static int		ANNOTATION	= 4;
	
	protected AbstractClass	theClass;
	
	private int				mode;
	private String			name;
	private IImplementable	implementable;
	
	public ClassBodyParser(AbstractClass theClass)
	{
		this.theClass = theClass;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (";".equals(value))
		{
			jcp.popParser();
		}
		else if (this.checkModifier(value))
		{
			;
		}
		else if ("(".equals(value))
		{
			if (this.mode == ANNOTATION)
			{
				jcp.pushParser(new AnnotationParser(this.annotation));
			}
			else
			{
				this.name = token.prev().value();
				
				this.type = this.body.getType();
				this.mode = METHOD;
				jcp.pushParser(new ParameterListParser(this.impl));
			}
		}
		else if ("{".equals(value))
		{
			this.mode = METHOD;
			jcp.pushParser(new CodeBlockParser(this.method));
		}
		else if ("}".equals(value))
		{
			jcp.popParser();
		}
		else if ("=".equals(value))
		{
			this.mode = VARIABLE;
			
			jcp.pushParser(new ValueParser(this.variable.getValue(), ";"));
		}
		else if (value.startsWith("@"))
		{
			this.mode = ANNOTATION;
			this.type = new AbstractClass();
			
		}
		else if ("throws".equals(value))
		{
			if (this.type != null)
			{
				this.mode = METHOD;
				jcp.pushParser(new ThrowsDeclParser(this.method));
			}
			else
			{
				this.mode = CONSTRUCTOR;
				jcp.pushParser(new ThrowsDeclParser(this.constructor));
			}
		}
		else
		{
			this.name = value;
		}
	}
	
	public void addMember() throws SyntaxException
	{
		if (this.mode == VARIABLE)
		{
			this.variable.setName(this.name);
			this.variable.setType(this.type);
			this.body.addVariable(this.variable);
			this.variable = new Variable();
		}
		else if (this.mode == METHOD)
		{
			this.method.setName(this.name);
			this.method.setType(this.type);
			this.body.addMethod(this.method);
			this.method = new Method();
		}
		else if (this.mode == CONSTRUCTOR)
		{
			this.body.addConstructor(this.constructor);
			this.constructor = new Constructor();
		}
		else if (this.mode == ANNOTATION)
		{
			
		}
	}
}
