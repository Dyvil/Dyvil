package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.lang.Name;

public interface IValueList extends Iterable<IValue>, IValueConsumer
{
	int size();
	
	default boolean isEmpty()
	{
		return this.size() == 0;
	}

	IValue get(int index);

	void set(int index, IValue value);

	void add(IValue value);

	default void add(Name name, IValue value)
	{
		this.add(value);
	}

	@Override
	default void setValue(IValue value)
	{
		this.add(value);
	}
}
