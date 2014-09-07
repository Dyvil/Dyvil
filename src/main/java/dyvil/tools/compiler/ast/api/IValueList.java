package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.value.IValue;

public interface IValueList
{
	public void setValues(List<IValue> list);
	
	public List<IValue> getValues();
	
	public default void addValue(IValue value)
	{
		this.getValues().add(value);
	}
}
