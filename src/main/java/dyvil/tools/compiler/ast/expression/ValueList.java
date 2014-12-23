package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ValueList extends ASTNode implements IValue, IValueList
{
	protected List<IValue>	values	= new ArrayList(3);
	
	protected boolean		isArray;
	
	public ValueList(ICodePosition position)
	{
		this.position = position;
	}
	
	public ValueList(ICodePosition position, boolean array)
	{
		this.position = position;
		this.isArray = array;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		if (this.values == null || this.values.isEmpty())
		{
			return Type.VOID;
		}
		IValue v = this.values.get(this.values.size() - 1);
		Type t = v.getType().clone();
		t.arrayDimensions++;
		return t;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.values = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.values.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.values;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values.get(index);
	}
	
	public boolean isEmpty()
	{
		return this.values.isEmpty();
	}
	
	@Override
	public void setArray(boolean array)
	{
		this.isArray = array;
	}
	
	@Override
	public boolean isArray()
	{
		return this.isArray;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		this.values.replaceAll(v -> v.applyState(state, context));
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		for (IValue ivalue : this.values)
		{
			ivalue.writeExpression(writer);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		for (IValue ivalue : this.values)
		{
			ivalue.writeExpression(writer);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isArray)
		{
			if (this.values.isEmpty())
			{
				buffer.append(Formatting.Expression.emptyArray);
			}
			else
			{
				buffer.append(Formatting.Expression.arrayStart);
				Util.astToString(this.values, Formatting.Expression.arraySeperator, buffer);
				buffer.append(Formatting.Expression.arrayEnd);
			}
		}
		else
		{
			if (this.values.isEmpty())
			{
				buffer.append(Formatting.Expression.emptyExpression);
			}
			else
			{
				buffer.append('\n').append(prefix).append('{').append('\n');
				for (IValue value : this.values)
				{
					buffer.append(prefix).append(Formatting.Method.indent);
					value.toString(prefix + Formatting.Method.indent, buffer);
					buffer.append(";\n");
				}
				buffer.append(prefix).append('}');
			}
		}
	}
}
