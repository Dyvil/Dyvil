package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.value.IValue;

public class Label
{
	public Name					name;
	public IValue					value;
	
	public org.objectweb.asm.Label	target	= new org.objectweb.asm.Label();
	
	public Label(Name name)
	{
		this.name = name;
	}
	
	public Label(Name name, IValue value)
	{
		this.name = name;
		this.value = value;
	}
	
	public Label(org.objectweb.asm.Label target)
	{
		this.target = target;
	}
	
	public Label(org.objectweb.asm.Label target, Name name)
	{
		this.target = target;
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return this.name != null ? this.name.qualified : this.target.info.toString();
	}
}
