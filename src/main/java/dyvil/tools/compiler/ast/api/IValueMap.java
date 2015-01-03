package dyvil.tools.compiler.ast.api;

import java.util.Map;

public interface IValueMap<T>
{
	public void setValues(Map<T, IValue> map);
	
	public void addValue(T key, IValue value);
	
	public Map<T, IValue> getValues();
	
	public IValue getValue(T key);
}
