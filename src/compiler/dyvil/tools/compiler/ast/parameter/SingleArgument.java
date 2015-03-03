package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;
import java.util.List;

import dyvil.collections.SingletonIterator;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public class SingleArgument implements IArguments, IValued
{
	private IValue	value;
	
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
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
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
	public IValue getValue(Parameter param)
	{
		return param.index == 0 ? this.value : null;
	}
	
	@Override
	public IType getType(Parameter param)
	{
		return param.index == 0 ? this.value.getType() : Type.NONE;
	}
	
	@Override
	public void writeValue(Parameter param, MethodWriter writer)
	{
		if (param.index == 0)
		{
			this.value.writeExpression(writer);
		}
	}
	
	@Override
	public int getTypeMatch(Parameter param)
	{
		return param.index == 0 ? this.value.getTypeMatch(param.type) : 0;
	}
	
	@Override
	public int getVarargsTypeMatch(Parameter param)
	{
		if (param.index == 0)
		{
			int m = this.value.getTypeMatch(param.type);
			if (m != 0)
			{
				return m;
			}
			return this.value.getTypeMatch(param.type.getElementType());
		}
		return 0;
	}
	
	@Override
	public void checkValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
		
	}
	
	@Override
	public void checkVarargsValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
		
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new SingletonIterator<IValue>(this.value);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.value.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(' ');
		this.value.toString(prefix, buffer);
	}
}
