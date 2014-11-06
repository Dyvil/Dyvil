package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.value.IValue;

public interface IValueList
{
	public void setValues(List<IValue> list);
	
	public List<IValue> getValues();
	
	public IValue getValue(int index);
	
	public void addValue(IValue value);
	
	public void setValue(int index, IValue value);
	
	public void setIsArray(boolean isArray);
	
	public boolean isArray();
}
