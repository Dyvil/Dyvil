package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class ArgumentList implements IArguments, IValueList
{
	private IValue[]	values;
	private int			size;
	
	private boolean varargs;
	
	public ArgumentList()
	{
		this.values = new IValue[3];
	}
	
	public ArgumentList(int size)
	{
		this.values = new IValue[size];
	}
	
	public ArgumentList(IValue[] values, int size)
	{
		this.values = values;
		this.size = size;
	}
	
	public IValue[] getValues()
	{
		return this.values;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values, this.size);
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public int valueCount()
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
		int len1 = this.size - 1;
		IValue[] values = new IValue[len1];
		System.arraycopy(this.values, 1, values, 0, len1);
		return new ArgumentList(values, len1);
	}
	
	@Override
	public IArguments withLastValue(IValue value)
	{
		IValue[] values = new IValue[this.size + 1];
		System.arraycopy(this.values, 0, values, 0, this.size);
		values[this.size] = value;
		return new ArgumentList(values, this.size + 1);
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
		this.values[index] = value;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.size++;
		if (this.size > this.values.length)
		{
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		int i = this.size++;
		if (this.size > this.values.length)
		{
			int j = index + 1;
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			temp[index] = value;
			System.arraycopy(this.values, j, temp, j, i - j);
			this.values = temp;
		}
		else
		{
			System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
			this.values[index] = value;
		}
	}
	
	@Override
	public IValue getValue(int index)
	{
		if (index >= this.size)
		{
			return null;
		}
		return this.values[index];
	}
	
	@Override
	public IValue getValue(int index, IParameter param)
	{
		return this.values[index];
	}
	
	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		if (index >= this.size)
		{
			return param.getValue() != null ? DEFAULT_MATCH : 0;
		}
		
		return this.values[index].getTypeMatch(param.getType());
	}
	
	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		if (index == this.size)
		{
			return DEFAULT_MATCH;
		}
		if (index > this.size)
		{
			return 0;
		}
		
		IValue argument = this.values[index];
		IType type = param.getType();
		float m = argument.getTypeMatch(type);
		if (m > 0F)
		{
			return m;
		}
		return argument.getTypeMatch(type.getElementType());
	}
	
	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			return;
		}
		
		IType type = param.getActualType();
		IValue value = this.values[index];
		IValue value1 = IType.convertValue(value, type, typeContext, markers, context);
		if (value1 == null)
		{
			Util.createTypeError(markers, value, type, typeContext, "method.access.argument_type", param.getName());
		}
		else
		{
			this.values[index] = value1;
		}
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			return;
		}
		
		IType varParamType = param.getActualType();
		
		IValue value = this.values[index];
		if (value.isType(varParamType.getConcreteType(typeContext)))
		{
			IValue value1 = IType.convertValue(value, varParamType, typeContext, markers, context);
			if (value1 != null)
			{
				this.values[index] = value1;
				this.varargs = true;
				return;
			}
			Util.createTypeError(markers, value, varParamType, typeContext, "method.access.argument_type", param.getName());
			return;
		}
		
		IType elementType = varParamType.getElementType();
		
		for (; index < this.size; index++)
		{
			value = this.values[index];
			IValue value1 = IType.convertValue(value, elementType, typeContext, markers, context);
			if (value1 == null)
			{
				Util.createTypeError(markers, value, elementType, typeContext, "method.access.argument_type", param.getName());
			}
			else
			{
				this.values[index] = value1;
			}
		}
	}
	
	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index >= this.size)
		{
			return;
		}
		param.getType().inferTypes(this.values[index].getType(), typeContext);
	}
	
	@Override
	public void inferVarargsType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index >= this.size)
		{
			return;
		}
		
		IType type = this.values[index].getType();
		if (index + 1 == this.size && type.isArrayType())
		{
			param.getType().inferTypes(type, typeContext);
			return;
		}
		
		for (int i = index + 1; i < this.size; i++)
		{
			type = Types.combine(type, this.values[i].getType());
		}
		
		param.getType().getElementType().inferTypes(type, typeContext);
	}
	
	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index < this.size)
		{
			this.values[index].writeExpression(writer, param.getType());
			return;
		}
		param.getValue().writeExpression(writer, param.getType());
	}
	
	@Override
	public void writeVarargsValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (this.varargs)
		{
			this.values[index].writeExpression(writer, param.getType());
			return;
		}
		
		IType type = param.getType().getElementType();
		int len = this.size - index;
		if (len < 0)
		{
			writer.writeLDC(0);
			writer.writeNewArray(type, 1);
			return;
		}
		
		int opcode = type.getArrayStoreOpcode();
		
		writer.writeLDC(len);
		writer.writeNewArray(type, 1);
		
		for (int i = 0; i < len; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			IValue value = this.values[index + i];
			writer.writeLDC(i);
			value.writeExpression(writer, type);
			writer.writeInsn(opcode);
		}
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
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			this.values[i].toString(prefix, buffer);
			if (i + 1 == len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}
	
	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			IType type = this.values[i].getType();
			if (type == null)
			{
				buffer.append("unknown");
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
