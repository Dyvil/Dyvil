package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;

public class Label
{
	public Name		name;
	public IValue	value;
	
	public dyvil.tools.asm.Label target;
	
	public Label(Name name)
	{
		this.name = name;
	}
	
	public Label(Name name, IValue value)
	{
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.name != null ? this.name.qualified : this.target.info.toString();
	}
}
