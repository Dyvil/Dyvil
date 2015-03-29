package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import dyvil.collections.ArrayIterator;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ArgumentMap implements IArguments, IValueMap
{
	private Name[]		keys	= new Name[3];
	private IValue[]	values	= new IValue[3];
	private int			size;
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values, this.size);
	}
	
	public Iterator<KeyValuePair> entryIterator()
	{
		return new Iterator<KeyValuePair>()
		{
			private int	index;
			
			@Override
			public KeyValuePair next()
			{
				if (this.index >= ArgumentMap.this.size)
				{
					throw new NoSuchElementException("ArrayIterator.next()");
				}
				int index = this.index++;
				return new KeyValuePair(ArgumentMap.this.keys[index], ArgumentMap.this.values[index]);
			}
			
			@Override
			public boolean hasNext()
			{
				return this.index < this.index;
			}
		};
	}
	
	@Override
	public void addValue(Name key, IValue value)
	{
		int index = this.size++;
		if (index >= this.values.length)
		{
			Name[] k = new Name[this.size];
			IValue[] v = new IValue[this.size];
			System.arraycopy(this.keys, 0, k, 0, index);
			System.arraycopy(this.values, 0, v, 0, index);
			this.keys = k;
			this.values = v;
		}
		this.values[index] = value;
		this.keys[index] = key;
	}
	
	@Override
	public IValue getValue(Name key)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				return this.values[i];
			}
		}
		return null;
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public IArguments dropFirstValue()
	{
		return this;
	}
	
	@Override
	public IArguments addLastValue(IValue value)
	{
		return this;
	}
	
	@Override
	public IArguments addLastValue(Name name, IValue value)
	{
		int size = this.size;
		int index = size++;
		Name[] k = new Name[size];
		IValue[] v = new IValue[size];
		System.arraycopy(this.keys, 0, k, 0, size);
		System.arraycopy(this.values, 0, v, 0, size);
		k[index] = name;
		v[index] = value;
		ArgumentMap map = new ArgumentMap();
		map.keys = k;
		map.values = v;
		map.size = size;
		return map;
	}
	
	@Override
	public IValue getFirstValue()
	{
		return this.values[0];
	}
	
	@Override
	public void setFirstValue(IValue value)
	{
		this.values[0] = value;
	}
	
	@Override
	public void setValue(int index, Parameter param, IValue value)
	{
		Name key = param.name;
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				this.values[i] = value;
				return;
			}
		}
	}
	
	@Override
	public IValue getValue(int index, Parameter param)
	{
		return this.getValue(param.name);
	}
	
	@Override
	public IType getType(int index, Parameter param)
	{
		return this.getValue(param.name).getType();
	}
	
	@Override
	public int getTypeMatch(int index, Parameter param)
	{
		Name key = param.name;
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				return this.values[i].getTypeMatch(param.type);
			}
		}
		return param.defaultValue != null ? 3 : 0;
	}
	
	@Override
	public int getVarargsTypeMatch(int index, Parameter param)
	{
		return this.getTypeMatch(index, param);
	}
	
	@Override
	public void checkValue(int index, Parameter param, MarkerList markers, ITypeContext context)
	{
		Name key = param.name;
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				IType type = param.type.getConcreteType(context);
				IValue value = this.values[i];
				IValue value1 = value.withType(type);
				if (value1 == null)
				{
					Marker marker = markers.create(value.getPosition(), "access.method.argument_type", param.name);
					marker.addInfo("Required Type: " + type);
					marker.addInfo("Value Type: " + value.getType());
				}
				else
				{
					this.values[i] = value;
				}
				return;
			}
		}
	}
	
	@Override
	public void checkVarargsValue(int index, Parameter param, MarkerList markers, ITypeContext context)
	{
		this.checkValue(index, param, markers, context);
	}
	
	@Override
	public void writeValue(int index, Name name, IValue defaultValue, MethodWriter writer)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == name)
			{
				this.values[i].writeExpression(writer);
				return;
			}
		}
		
		defaultValue.writeExpression(writer);
	}
	
	@Override
	public void writeVarargsValue(int index, Name name, IType type, MethodWriter writer)
	{
		this.writeValue(index, name, null, writer);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			Name key = this.keys[i];
			IValue v = this.values[i];
			buffer.append(key).append(Formatting.Method.keyValueSeperator);
			
			if (v == null)
			{
				buffer.append("[null-value]");
			}
			else
			{
				v.toString(prefix, buffer);
			}
			if (i + 1 == len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}
}
