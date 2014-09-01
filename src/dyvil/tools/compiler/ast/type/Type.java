package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CodeParser;
import dyvil.tools.compiler.ast.classes.AbstractClass;

public class Type
{
	public static Type		VOID	= new Type("void");
	public static Type		INT		= new Type("int");
	public static Type		LONG	= new Type("long");
	public static Type		FLOAT	= new Type("float");
	public static Type		DOUBLE	= new Type("double");
	public static Type		CHAR	= new Type("char");
	public static Type		BOOL	= new Type("boolean");
	
	public static Type		STRING	= new Type("java.lang.String");
	
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
