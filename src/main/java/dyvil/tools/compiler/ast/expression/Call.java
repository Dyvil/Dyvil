package dyvil.tools.compiler.ast.expression;

import java.util.List;

import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.value.IValue;

public abstract class Call implements IValue, IValueList
{
	protected List<IValue>	arguments;
	
	protected boolean		isSugarCall;
	
	public IMethod			descriptor;
	
	@Override
	public IValue fold()
	{
		for (int i = 0; i < this.arguments.size(); i++)
		{
			IValue value = this.arguments.get(i);
			IValue value1 = value.fold();
			if (value != value1)
			{
				this.arguments.set(i, value1);
			}
		}
		return this;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
}
