package dyvil.tools.compiler.ast.value;

public interface IValueMap<T>
{
	public void addValue(T key, IValue value);
	
	public IValue getValue(T key);
}
