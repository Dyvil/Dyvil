package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;

public class TupleValue extends ASTNode implements IValue, IValueList
{
	private List<IValue>	values;
	
	public TupleValue()
	{
		this.values = new ArrayList(3);
	}
	
	public TupleValue(List<IValue> values)
	{
		this.values = values;
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
	
	@Override
	public void addValue(IValue value)
	{
		this.values.add(value);
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values.get(index);
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values.set(index, value);
	}
	
	@Override
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		// TODO Type
		return null;
	}
	
	@Override
	public Object toObject()
	{
		// TODO Tuple object
		return null;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (this.values.size() == 1)
			{
				return this.values.get(0).applyState(state, context);
			}
		}
		Util.applyState(this.values, state, context);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.values, buffer, true, Formatting.Expression.emptyTuple, Formatting.Expression.tupleStart, Formatting.Expression.tupleSeperator, Formatting.Expression.tupleEnd);
	}
	
	@Override
	public void writeExpression(MethodWriter visitor)
	{
		// TODO
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		// TODO
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}
}
