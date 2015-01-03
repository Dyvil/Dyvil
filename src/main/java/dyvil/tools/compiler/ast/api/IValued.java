package dyvil.tools.compiler.ast.api;

public interface IValued
{
	public void setValue(IValue value);
	
	public IValue getValue();
	
	public default boolean hasValue()
	{
		return this.getValue() != null;
	}
}
