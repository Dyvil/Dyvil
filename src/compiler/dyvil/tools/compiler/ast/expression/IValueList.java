package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.statement.control.Label;

public interface IValueList extends Iterable<IValue>, IValueConsumer
{
	int valueCount();
	
	default boolean isEmpty()
	{
		return this.valueCount() == 0;
	}
	
	void setValue(int index, IValue value);
	
	void addValue(IValue value);
	
	default void addValue(IValue value, Label label)
	{
		this.addValue(value);
	}
	
	void addValue(int index, IValue value);
	
	IValue getValue(int index);

	@Override
	default void setValue(IValue value)
	{
		this.addValue(value);
	}
}
