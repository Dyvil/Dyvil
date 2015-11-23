package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueMap;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public final class ArgumentMap implements IArguments, IValueMap
{
	private Name[]   keys   = new Name[3];
	private IValue[] values = new IValue[3];
	private int size;
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, this.size);
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
	public IArguments withLastValue(IValue value)
	{
		return this;
	}
	
	@Override
	public IArguments withLastValue(Name name, IValue value)
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
	public IValue getLastValue()
	{
		return this.values[this.size - 1];
	}
	
	@Override
	public void setLastValue(IValue value)
	{
		this.values[this.size - 1] = value;
	}
	
	@Override
	public void setValue(int index, IParameter param, IValue value)
	{
		if (param == null)
		{
			this.values[index] = value;
			return;
		}

		Name key = param.getName();
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
	public IValue getValue(int index, IParameter param)
	{
		if (param == null)
		{
			return this.values[index];
		}

		return this.getValue(param.getName());
	}
	
	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		Name key = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				return this.values[i].getTypeMatch(param.getType());
			}
		}
		return param.getValue() != null ? DEFAULT_MATCH : 0;
	}
	
	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		return this.getTypeMatch(index, param);
	}
	
	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		Name key = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] != key)
			{
				continue;
			}
			
			IType type = param.getActualType().getParameterType();
			IValue value = this.values[i];
			IValue typed = IType.convertValue(value, type, typeContext, markers, context);
			if (typed == null)
			{
				Util.createTypeError(markers, value, type, typeContext, "method.access.argument_type", key);
			}
			else
			{
				this.values[i] = typed;
			}
			return;
		}
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.checkValue(index, param, typeContext, markers, context);
	}
	
	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		IType type = param.getType();
		Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == name)
			{
				type.inferTypes(this.values[i].getType(), typeContext);
			}
		}
	}
	
	@Override
	public void inferVarargsType(int index, IParameter param, ITypeContext typeContext)
	{
		this.inferType(index, param, typeContext);
	}
	
	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == name)
			{
				this.values[i].writeExpression(writer, param.getType());
				return;
			}
		}
		
		param.getValue().writeExpression(writer, param.getType());
	}
	
	@Override
	public void writeVarargsValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		this.writeValue(index, param, writer);
	}

	@Override
	public boolean isResolved()
	{
		for (int i = 0; i < this.size; i++)
		{
			if (!this.values[i].isResolved())
			{
				return false;
			}
		}

		return true;
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
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
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			buffer.append(this.keys[i]);
			Formatting.appendSeparator(buffer, "parameters.name_value_separator", ':');
			this.values[i].toString(prefix, buffer);
			if (i + 1 == len)
			{
				break;
			}
			Formatting.appendSeparator(buffer, "parameters.separator", ',');
		}

		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}
	
	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			IType type = this.values[i].getType();
			buffer.append(this.keys[i]).append(": ");
			
			if (type == null)
			{
				buffer.append("_");
			}
			else
			{
				type.toString("", buffer);
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
