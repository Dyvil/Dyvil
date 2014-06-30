package dyvil.tools.compiler.ast.api;

import java.util.List;

public interface IValueList
{
	public void setValues(List<Object> list);
	
	public List<Object> getValues();
	
	public default void addValue(Object value)
	{
		this.getValues().add(value);
	}
}
