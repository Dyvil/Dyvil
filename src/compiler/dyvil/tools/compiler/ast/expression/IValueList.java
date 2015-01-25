package dyvil.tools.compiler.ast.expression;

import java.util.List;

public interface IValueList
{
	public void setValues(List<IValue> list);
	
	public void setValue(int index, IValue value);
	
	public void addValue(IValue value);
	
	public List<IValue> getValues();
	
	public IValue getValue(int index);
	
	public default void setArray(boolean array)
	{
	}
	
	public default boolean isArray()
	{
		return false;
	}
	
	public default void addLabel(String name, IValue value)
	{
	}
}
