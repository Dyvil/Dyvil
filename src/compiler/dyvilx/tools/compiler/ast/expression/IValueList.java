package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Name;

public interface IValueList extends Iterable<IValue>
{
	int size();

	default boolean isEmpty()
	{
		return this.size() == 0;
	}

	IValue get(int index);

	void set(int index, IValue value);

	void add(IValue value);

	default void add(Name label, IValue value)
	{
		this.add(value);
	}
}
