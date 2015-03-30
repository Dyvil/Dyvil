package dyvil.tools.compiler.ast.parameter;

import java.util.Collections;
import java.util.Iterator;

import dyvil.collections.SingletonIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class SingleArgument implements IArguments, IValued
{
	private IValue	value;
	private boolean	varargs;
	
	public SingleArgument()
	{
	}
	
	public SingleArgument(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public int size()
	{
		return this.value != null ? 1 : 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.value == null;
	}
	
	// 'Variations'
	
	@Override
	public IArguments dropFirstValue()
	{
		return EmptyArguments.INSTANCE;
	}
	
	@Override
	public IArguments addLastValue(IValue value)
	{
		ArgumentList list = new ArgumentList();
		list.addValue(this.value);
		list.addValue(value);
		return list;
	}
	
	// First Values
	
	@Override
	public IValue getFirstValue()
	{
		return this.value;
	}
	
	@Override
	public void setFirstValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	// Used by Methods
	
	@Override
	public void setValue(int index, IParameter param, IValue value)
	{
		if (index == 0)
		{
			this.value = value;
		}
	}
	
	@Override
	public IValue getValue(int index, IParameter param)
	{
		return index == 0 ? this.value : null;
	}
	
	@Override
	public IType getType(int index, IParameter param)
	{
		return index == 0 ? this.value.getType() : Type.NONE;
	}
	
	@Override
	public int getTypeMatch(int index, IParameter param)
	{
		if (index == 0)
		{
			return this.value.getTypeMatch(param.getType());
		}
		return param.getValue() != null ? 3 : 0;
	}
	
	@Override
	public int getVarargsTypeMatch(int index, IParameter param)
	{
		if (index != 0)
		{
			return 3;
		}
		
		int m = this.value.getTypeMatch(param.getType());
		if (m != 0)
		{
			return m;
		}
		return this.value.getTypeMatch(param.getType().getElementType());
	}
	
	@Override
	public void checkValue(int index, IParameter param, MarkerList markers, ITypeContext context)
	{
		if (index != 0)
		{
			return;
		}
		this.value = this.value.withType(param.getType(context));
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, MarkerList markers, ITypeContext context)
	{
		if (index != 0)
		{
			return;
		}
		
		IType type = param.getType(context);
		IValue value1 = this.value.withType(type);
		if (value1 != null)
		{
			this.value = value1;
			this.varargs = true;
			return;
		}
		
		this.value = this.value.withType(type.getElementType());
	}
	
	@Override
	public void writeValue(int index, Name name, IValue defaultValue, MethodWriter writer)
	{
		if (index == 0)
		{
			this.value.writeExpression(writer);
			return;
		}
		
		defaultValue.writeExpression(writer);
	}
	
	@Override
	public void writeVarargsValue(int index, Name name, IType type, MethodWriter writer)
	{
		if (index != 0)
		{
			return;
		}
		if (this.varargs)
		{
			// Write the value as is (it is an array)
			this.value.writeExpression(writer);
			return;
		}
		
		// Write an array with one element
		type = type.getElementType();
		writer.writeLDC(1);
		writer.writeNewArray(type, 1);
		writer.writeInsn(Opcodes.DUP);
		writer.writeLDC(0);
		this.value.writeExpression(writer);
		writer.writeInsn(type.getArrayStoreOpcode());
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		if (this.value == null)
		{
			return Collections.emptyIterator();
		}
		
		return new SingletonIterator<IValue>(this.value);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.value == null)
		{
			return;
		}
		
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value == null)
		{
			return;
		}
		
		buffer.append(' ');
		this.value.toString(prefix, buffer);
	}
}
