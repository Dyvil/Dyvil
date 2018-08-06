package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.expression.IValue;

public class NamedArgumentList extends ArgumentList
{
	public NamedArgumentList()
	{
		super();
	}

	public NamedArgumentList(int capacity)
	{
		super(capacity);
	}

	public NamedArgumentList(Name[] keys, IValue[] values, int size)
	{
		super(keys, values, size);
	}
}
