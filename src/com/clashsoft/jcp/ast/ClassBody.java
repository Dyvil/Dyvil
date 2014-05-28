package com.clashsoft.jcp.ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.ast.member.Variable;
import com.clashsoft.jcp.ast.member.methods.Constructor;
import com.clashsoft.jcp.ast.member.methods.Method;

public class ClassBody
{
	private Map<String, Variable> variables = new HashMap();
	private Map<String, Method> methods = new HashMap();
	private List<Constructor> constructors = new LinkedList();
	
	public void addVariable(Variable var) throws SyntaxException
	{
		String key = var.getName();
		this.variables.put(key, var);
	}
	
	public void addMethod(Method method) throws SyntaxException
	{
		String key = method.getName();
		this.methods.put(key, method);
	}
	
	public void addConstructor(Constructor constructor) throws SyntaxException
	{
		this.constructors.add(constructor);
	}
}
