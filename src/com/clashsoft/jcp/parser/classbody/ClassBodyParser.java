package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.JCPHelper;
import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.ClassBody;
import com.clashsoft.jcp.ast.annotation.Annotation;
import com.clashsoft.jcp.ast.member.Variable;
import com.clashsoft.jcp.ast.member.methods.Constructor;
import com.clashsoft.jcp.ast.member.methods.Method;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class ClassBodyParser extends Parser
{
	public static int	VARIABLE	= 1;
	public static int	METHOD		= 2;
	public static int	CONSTRUCTOR	= 3;
	public static int	ANNOTATION	= 4;
	
	private ClassBody	body;
	
	private int			mode;
	private String		name;
	private String		type;
	
	private Variable	variable	= new Variable();
	private Method		method		= new Method();
	private Constructor	constructor	= new Constructor();
	private Annotation	annotation	= new Annotation();
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (";".equals(value))
		{
			jcp.popParser();
		}
		else if (this.checkModifier(value))
		{
			;
		}
		else if (JCPHelper.isPrimitiveType(value))
		{
			this.type = value;
		}
		else if ("(".equals(value))
		{
			if (this.mode == ANNOTATION)
			{
				jcp.pushParser(new AnnotationParser(this.annotation));
			}
			else
			{
				this.name = token.prev().value;
				
				if (this.type != null)
				{
					this.mode = METHOD;
					jcp.pushParser(new ParameterParser(this.method));
				}
				else
				{
					this.mode = CONSTRUCTOR;
					jcp.pushParser(new ParameterParser(this.constructor));
				}
			}
		}
		else if ("{".equals(value))
		{
			if (this.type != null)
			{
				this.mode = METHOD;
				jcp.pushParser(new ImplementationParser(this.method));
			}
			else
			{
				this.mode = CONSTRUCTOR;
				jcp.pushParser(new ImplementationParser(this.constructor));
			}
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
			this.type = value;
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
