package dyvil.tools.compiler.ast.statement.control;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.lang.Name;

public class Label
{
	public  Name                  name;
	public  IValue                value;
	private dyvil.tools.asm.Label target;

	public Label(Name name)
	{
		this.name = name;
	}

	public Label(Name name, IValue value)
	{
		this.name = name;
		this.value = value;
	}

	public dyvil.tools.asm.Label getTarget()
	{
		if (this.target != null)
		{
			return this.target;
		}
		return this.target = new dyvil.tools.asm.Label();
	}

	public void setTarget(dyvil.tools.asm.Label target)
	{
		this.target = target;
	}

	@Override
	public String toString()
	{
		return this.name != null ? this.name.qualified : this.target.info.toString();
	}
}
