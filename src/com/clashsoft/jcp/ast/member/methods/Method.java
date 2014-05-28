package com.clashsoft.jcp.ast.member.methods;

import java.util.*;

import com.clashsoft.jcp.ast.member.Implementation;
import com.clashsoft.jcp.ast.member.Member;
import com.clashsoft.jcp.ast.member.Parameter;

public class Method extends Member implements IThrower, IParameterized, IImplementable
{
	private Implementation implementation;
	
	private Map<String, Parameter> parameters = new HashMap();
	private List<ThrowsDecl> throwsDecl = new LinkedList();
	
	@Override
	public void setImplementation(Implementation implementation)
	{
		this.implementation = implementation;
	}
	
	@Override
	public boolean addParameter(Parameter parameter)
	{
		return this.parameters.put(parameter.getName(), parameter) != null;
	}
	
	@Override
	public boolean addThrowsDecl(ThrowsDecl throwsDecl)
	{
		return this.throwsDecl.add(throwsDecl);
	}
	
	public Implementation getImplementation()
	{
		return this.implementation;
	}
	
	public Map<String, Parameter> getParameters()
	{
		return this.parameters;
	}
	
	public List<ThrowsDecl> getThrowsDecl()
	{
		return this.throwsDecl;
	}
}
