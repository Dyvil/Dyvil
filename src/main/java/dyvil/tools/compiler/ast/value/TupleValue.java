package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;

public class TupleValue extends ASTObject implements IValue, IValueList
{
	private List<IValue>	values	= new ArrayList();
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public IValue fold()
	{
		if (this.values.size() == 1)
		{
			return this.values.get(0);
		}
		return this;
	}
	
	@Override
	public Type getType()
	{
		// TODO Type
		return null;
	}
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.values.isEmpty())
		{
			buffer.append(Formatting.Expression.emptyTuple);
		}
		else
		{
			buffer.append(Formatting.Expression.tupleStart);
			Iterator<IValue> iterator = this.values.iterator();
			while (true)
			{
				IValue value = iterator.next();
				value.toString("", buffer);
				if (iterator.hasNext())
				{
					buffer.append(Formatting.Expression.tupleSeperator);
				}
				else
				{
					break;
				}
			}
			buffer.append(Formatting.Expression.tupleEnd);
		}
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.values = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.values;
	}
	
}
