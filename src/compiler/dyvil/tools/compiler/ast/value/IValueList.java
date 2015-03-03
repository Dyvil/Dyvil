package dyvil.tools.compiler.ast.value;

public interface IValueList extends Iterable<IValue>
{
	public void setValue(int index, IValue value);
	
	public void addValue(IValue value);
	
	public void addValue(int index, IValue value);
	
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
