package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.annotation.Annotation;
import com.clashsoft.jcp.ast.member.Variable;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class AnnotationParser extends Parser
{
	private Annotation annotation;
	private Variable parameter;
	
	private String name;
	
	public AnnotationParser(Annotation annotation)
	{
		this.annotation = annotation;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (",".equals(value))
		{
			this.parameter.setName(this.name);
			this.annotation.addParameter(this.parameter);
			this.parameter = new Variable();
		}
		else if (")".equals(value))
		{
			jcp.popParser();
		}
		else if (!"=".equals(value))
		{
			if (this.name == null)
			{
				this.name = value;
			}
			else
			{
				jcp.pushParser(new ValueParser(this.parameter.getValue(), ","));
			}
		}
	}
}
