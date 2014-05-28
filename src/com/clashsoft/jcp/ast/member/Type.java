package com.clashsoft.jcp.ast.member;

public class Type
{
	private String className;
	private int arrayDimensions;
	
	public Type()
	{
	}
	
	public void setClassName(String className)
	{
		this.className= className;
	}
	
	public void setArrayDimensions(int dims)
	{
		this.arrayDimensions = dims;
	}
	
	public void incrArrayDimensions()
	{
		this.arrayDimensions++;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
}
