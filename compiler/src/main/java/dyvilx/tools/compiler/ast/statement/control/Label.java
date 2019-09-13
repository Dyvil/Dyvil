package dyvilx.tools.compiler.ast.statement.control;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvil.lang.Name;

public class Label
{
	public  Name                  name;
	public  IValue                value;
	private dyvilx.tools.asm.Label target;

	public Label(Name name)
	{
		this.name = name;
	}

	public Label(Name name, IValue value)
	{
		this.name = name;
		this.value = value;
	}

	public dyvilx.tools.asm.Label getTarget()
	{
		if (this.target != null)
		{
			return this.target;
		}
		return this.target = new dyvilx.tools.asm.Label();
	}

	public void setTarget(dyvilx.tools.asm.Label target)
	{
		this.target = target;
	}

	@Override
	public String toString()
	{
		return this.name != null ? this.name.qualified : this.target.info.toString();
	}
}
