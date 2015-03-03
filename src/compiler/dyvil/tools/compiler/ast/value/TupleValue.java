package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
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
	public void addValue(int index, IValue value)
	{
		this.values.add(index, value);
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values.get(index);
	}

	@Override
	public Iterator<IValue> iterator()
	{
		return this.values.iterator();
	}
	
	@Override
	public IType getType()
	{
		int len = this.values.size();
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType(len);
		for (int i = 0; i < len; i++)
		{
			IValue v = this.values.get(i);
			t.addType(v.getType());
		}
		return this.tupleType = t;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this.isType(type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.values.size() == 1)
		{
			return this.values.get(0).isType(type);
		}
		
		IType type1 = this.getType();
		return Type.isSuperType(type, type1);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.values.size() == 1)
		{
			return this.values.get(0).getTypeMatch(type);
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		TupleType type = new TupleType();
		for (IValue v : this.values)
		{
			v.resolveTypes(markers, context);
			type.addType(v.getType());
		}
		type.getTheClass();
		this.tupleType = type;
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
		Util.parametersToString(prefix, this.values, buffer, true, Formatting.Expression.emptyTuple,
				Formatting.Expression.tupleStart, Formatting.Expression.tupleSeperator, Formatting.Expression.tupleEnd);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		TupleType t = this.tupleType;
		writer.visitTypeInsn(Opcodes.NEW, t);
		writer.visitInsn(Opcodes.DUP);
		
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
		for (IValue v : this.values)
		{
			v.writeStatement(writer);
		}
	}
}
