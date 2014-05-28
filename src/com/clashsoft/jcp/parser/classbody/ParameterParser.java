package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.member.Parameter;
import com.clashsoft.jcp.ast.member.Type;
import com.clashsoft.jcp.ast.member.methods.Method;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class ParameterParser extends Parser
{	
	private Method method;
	
	private String name;
	private Type type;
	
	public ParameterParser(Method method)
	{
		this.method = method;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (this.checkModifier(value))
		{
			;
		}
		else if (")".equals(value))
		{
			jcp.popParser();
		}
		else if (this.type == null)
		{
			this.type = new Type();
			jcp.pushParser(new TypeParser(this.type, ","));
		}
		else if (this.name == null)
		{
			this.name = value;
		}
		else if (",".equals(value))
		{
			this.method.addParameter(new Parameter(this.name, this.type, this.modifiers));
			this.name = null;
			this.type = null;
			this.modifiers = 0;
		}
	}
}
