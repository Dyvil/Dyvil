package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.value.IValue;

public class Label
{
	public String					name;
	public IValue					value;
	
	public org.objectweb.asm.Label	target	= new org.objectweb.asm.Label();
	
	public Label(String name)
	{
		this.name = name;
	}
	
	public Label(String name, IValue value)
	{
		this.name = name;
		this.value = value;
	}
}
