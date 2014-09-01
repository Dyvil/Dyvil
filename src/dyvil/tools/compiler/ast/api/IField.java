package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.value.IValue;

public interface IField
{
	public void setValue(IValue value);
	
	public IValue getValue();
	
	public default boolean hasValue()
	{
		return this.getValue() != null;
	}
}
