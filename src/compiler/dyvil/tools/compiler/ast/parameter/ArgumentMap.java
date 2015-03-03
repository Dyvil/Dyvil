package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;

public final class ArgumentMap implements IArguments, IValueMap<String>
{
	private String[]	keys	= new String[3];
	private IValue[]	values	= new IValue[3];
	private int			size;
	
	@Override
	public Iterator<IValue> iterator()
	{
		return null;
	}
	
	@Override
	public void addValue(String key, IValue value)
	{
		int index = this.size++;
		if (this.size > this.values.length)
		{
			String[] k = new String[this.size];
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
	public IValue getValue(String key)
	{
		int hash = key.hashCode();
		for (int i = 0; i < this.size; i++)
		{
			String s = keys[i];
			if (s.hashCode() == hash && s.equals(key))
			{
				return values[i];
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
		return this.size > 0;
	}
	
	@Override
	public IArguments dropFirstValue()
	{
		return null;
	}
	
	@Override
	public IArguments addLastValue(IValue value)
	{
		return null;
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
	public IValue getValue(Parameter param)
	{
		return this.getValue(param.qualifiedName);
	}
	
	@Override
	public IType getType(Parameter param)
	{
		return this.getValue(param.qualifiedName).getType();
	}
	
	@Override
	public void writeValue(Parameter param, MethodWriter writer)
	{
		String key = param.qualifiedName;
		int hash = key.hashCode();
		for (int i = 0; i < this.size; i++)
		{
			String s = keys[i];
			if (s.hashCode() == hash && s.equals(key))
			{
				values[i].writeExpression(writer);
				return;
			}
		}
		
		if (param.defaultValue != null)
		{
			param.defaultValue.writeExpression(writer);
		}
	}
	
	@Override
	public int getTypeMatch(Parameter param)
	{
		String key = param.qualifiedName;
		int hash = key.hashCode();
		for (int i = 0; i < this.size; i++)
		{
			String s = keys[i];
			if (s.hashCode() == hash && s.equals(key))
			{
				return values[i].getTypeMatch(param.type);
			}
		}
		return param.defaultValue != null ? 3 : 0;
	}
	
	@Override
	public int getVarargsTypeMatch(Parameter param)
	{
		return 0;
	}
	
	@Override
	public void checkValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
		String key = param.qualifiedName;
		int hash = key.hashCode();
		for (int i = 0; i < this.size; i++)
		{
			String s = keys[i];
			if (s.hashCode() == hash && s.equals(key))
			{
				IType type = param.getType(context);
				IValue value = this.values[i];
				IValue value1 = value.withType(type);
				if (value1 == null)
				{
					Marker marker = Markers.create(value.getPosition(), "access.method.argument_type", param.name);
					marker.addInfo("Required Type: " + type);
					marker.addInfo("Value Type: " + value.getType());
					markers.add(marker);
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
	public void checkVarargsValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
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
			String key = this.keys[i];
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
