package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.statement.Label;

public interface IValueList extends Iterable<IValue>
{
	public int valueCount();
	
	public boolean isEmpty();
	
	public void setValue(int index, IValue value);
	
	public void addValue(IValue value);
	
	public default void addValue(IValue value, Label label)
	{
		this.addValue(value);
	}
	
	public void addValue(int index, IValue value);
	
	public IValue getValue(int index);
}
