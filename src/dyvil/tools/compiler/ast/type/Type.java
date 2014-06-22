package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CodeParser;
import dyvil.tools.compiler.ast.classes.AbstractClass;

public class Type
{
	private AbstractClass	theClass;
	private char			seperator;
	private int				arrayDimensions;
	
	public Type(String name)
	{
		this.setName(name);
	}
	
	public void setClass(AbstractClass theClass)
	{
		this.theClass = theClass;
	}
	
	public void setName(String name)
	{
		this.theClass = CodeParser.resolveClass(name);
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	public AbstractClass getAbstractClass()
	{
		return this.theClass;
	}
	
	public String getName()
	{
		return this.theClass == null ? null : this.theClass.getName();
	}
	
	public char getSeperator()
	{
		return this.seperator;
	}
	
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
}
