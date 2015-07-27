package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;

public interface IValued extends IValueConsumer
{
	@Override
	public void setValue(IValue value);
	
	public IValue getValue();
	
	public default boolean hasValue()
	{
		return this.getValue() != null;
	}
}
