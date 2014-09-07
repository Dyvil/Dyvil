package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IClassContext;
import dyvil.tools.compiler.ast.method.IMethod;

public class Type implements IClassContext
{
	public static Type	VOID	= new Type("void");
	public static Type	INT		= new Type("int");
	public static Type	LONG	= new Type("long");
	public static Type	FLOAT	= new Type("float");
	public static Type	DOUBLE	= new Type("double");
	public static Type	CHAR	= new Type("char");
	public static Type	BOOL	= new Type("boolean");
	
	public static Type	STRING	= new Type("java.lang.String");
	public static Type	CLASS	= new Type("java.lang.Class");
	
	private IClass		theClass;
	private char		seperator;
	private int			arrayDimensions;
	
	public Type(String name)
	{
		// FIXME
	}
	
	public Type(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public void setClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	public IClass getTheClass()
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
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		return this.theClass.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		return this.theClass.resolveMethodName(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		return this.theClass.resolveMethod(name, args);
	}
}
