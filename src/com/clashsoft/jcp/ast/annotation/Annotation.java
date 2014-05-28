package com.clashsoft.jcp.ast.annotation;

import java.util.HashMap;
import java.util.Map;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.ast.member.Variable;

public class Annotation
{
	private String type;
	
	private Map<String, Variable> parameters = new HashMap();
	
	public void setType(String type) throws SyntaxException
	{
		if (this.type != null)
		{
			throw new SyntaxException("annotation.type.set", type);
		}
		this.type = type;
	}
	
	public void addParameter(Variable var) throws SyntaxException
	{
		String key = var.getName();
		this.parameters.put(key, var);
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public Map<String, Variable> getParameters()
	{
		return this.parameters;
	}
}
