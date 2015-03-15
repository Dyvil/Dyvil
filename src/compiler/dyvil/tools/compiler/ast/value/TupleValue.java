package dyvil.tools.compiler.ast.value;

import java.util.Iterator;

import dyvil.collections.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class TupleValue extends ASTNode implements IValue, IValueList
{
	private IValue[]	values;
	private int			valueCount;
	
	private TupleType	tupleType;
	
	public TupleValue(ICodePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
	}
	
	public TupleValue(ICodePosition position, IValue[] values)
	{
		this.position = position;
		this.values = values;
		this.valueCount = values.length;
	}
	
	@Override
	public int getValueType()
	{
		return TUPLE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values);
	}
	
	@Override
	public int valueCount()
	{
		return this.valueCount;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.valueCount++;
		if (this.valueCount > this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		int i = this.valueCount++;
		System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
		this.values[index] = value;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}
	
	@Override
	public TupleType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType(this.valueCount);
		for (int i = 0; i < this.valueCount; i++)
		{
			t.addType(this.values[i].getType());
		}
		return this.tupleType = t;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].withType(type);
		}
		
		if (TupleType.isSuperType(type, this.values, this.valueCount))
		{
			this.getType();
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].isType(type);
		}
		
		return TupleType.isSuperType(type, this.values, this.valueCount);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].getTypeMatch(type);
		}
		
		IType t = this.getType();
		if (type.equals(t))
		{
			return 3;
		}
		else if (Type.isSuperType(type, t))
		{
			return 2;
		}
		else if (type.classEquals(Type.OBJECT))
		{
			return 1;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		TupleType type = new TupleType();
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v = this.values[i];
			v.resolveTypes(markers, context);
			type.addType(v.getType());
		}
		type.getTheClass();
		this.tupleType = type;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].resolve(markers, context);
		}
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		TupleType t = this.tupleType;
		writer.writeTypeInsn(Opcodes.NEW, t);
		writer.writeInsn(Opcodes.DUP);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].writeExpression(writer);
		}
		
		String owner = t.getInternalName();
		String desc = t.getConstructorDescriptor();
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false, this.valueCount + 1, t);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].writeStatement(writer);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.values, this.valueCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}
