package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class TupleValue extends ASTNode implements IValue, IValueList
{
	private List<IValue>	values;
	private TupleType		tupleType;
	
	public TupleValue(ICodePosition position)
	{
		this.position = position;
		this.values = new ArrayList(3);
	}
	
	public TupleValue(ICodePosition position, List<IValue> values)
	{
		this.position = position;
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
	public TupleType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType();
		for (IValue v : this.values)
		{
			t.addType(v.getType());
		}
		return this.tupleType = t;
	}
	
	@Override
	public int getValueType()
	{
		return TUPLE;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.getType();
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.values.size() == 1)
		{
			return this.values.get(0).resolve(markers, context);
		}
		
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.resolve(markers, context);
			if (v1 != v2)
			{
				this.values.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (IValue v : this.values)
		{
			v.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.values.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.values, buffer, true, Formatting.Expression.emptyTuple, Formatting.Expression.tupleStart, Formatting.Expression.tupleSeperator, Formatting.Expression.tupleEnd);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		TupleType t = this.getType();
		writer.visitTypeInsn(Opcodes.NEW, t);
		writer.visitInsn(Opcodes.DUP, t);
		
		for (IValue v : this.values)
		{
			v.writeExpression(writer);
		}
		
		int args = this.values.size() + 1;
		String owner = t.getInternalName();
		String desc = t.getConstructorDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false, args, t);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
}
