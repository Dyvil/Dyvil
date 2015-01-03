package dyvil.tools.compiler.ast.api;

import java.util.List;

public interface IValueList
{
	public void setValues(List<IValue> list);
	
	public void setValue(int index, IValue value);
	
	public void addValue(IValue value);
	
	public List<IValue> getValues();
	
	public IValue getValue(int index);
	
	public void setArray(boolean array);
	
	public boolean isArray();
}
