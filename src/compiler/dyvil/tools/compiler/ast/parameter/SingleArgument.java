package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.EmptyIterator;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public final class SingleArgument implements IArguments, IValueConsumer
{
	private IValue  value;
	private boolean varargs;
	
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
		return this.value == null ? 0 : 1;
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
	public IArguments withLastValue(IValue value)
	{
		if (this.value == null)
		{
			return new SingleArgument(value);
		}
		
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
	public IValue getLastValue()
	{
		return this.value;
	}
	
	@Override
	public void setLastValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
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
	public float getTypeMatch(int index, IParameter param)
	{
		if (index == 0 && this.value != null)
		{
			return this.value.getTypeMatch(param.getType());
		}
		return param.getValue() != null ? DEFAULT_MATCH : 0;
	}
	
	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		if (index == 1)
		{
			return DEFAULT_MATCH;
		}
		if (index > 1 || this.value == null)
		{
			return 0;
		}
		
		float m = this.value.getTypeMatch(param.getType());
		if (m != 0)
		{
			return m;
		}
		return this.value.getTypeMatch(param.getType().getElementType());
	}
	
	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		
		IType type = param.getActualType().getParameterType();
		IValue typed = IType.convertValue(this.value, type, typeContext, markers, context);
		if (typed == null)
		{
			Util.createTypeError(markers, this.value, type, typeContext, "method.access.argument_type",
			                     param.getName());
		}
		else
		{
			this.value = typed;
		}
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		
		IType type = param.getActualType();
		IValue value1 = IType.convertValue(this.value, type, typeContext, markers, context);
		if (value1 != null)
		{
			this.value = value1;
			this.varargs = true;
			return;
		}
		
		value1 = IType.convertValue(this.value, type.getElementType(), typeContext, markers, context);
		if (value1 == null)
		{
			Util.createTypeError(markers, this.value, type, typeContext, "method.access.argument_type",
			                     param.getName());
		}
		else
		{
			this.value = value1;
		}
	}
	
	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index == 0 && this.value != null)
		{
			param.getType().inferTypes(this.value.getType(), typeContext);
		}
	}
	
	@Override
	public void inferVarargsType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		
		IType type = this.value.getType();
		if (type.isArrayType())
		{
			param.getType().inferTypes(type, typeContext);
			return;
		}
		
		param.getType().getElementType().inferTypes(type, typeContext);
	}
	
	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index == 0 && this.value != null)
		{
			this.value.writeExpression(writer, param.getType());
			return;
		}
		
		param.getValue().writeExpression(writer, param.getType());
	}
	
	@Override
	public void writeVarargsValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		if (this.varargs)
		{
			// Write the value as is (it is an array)
			this.value.writeExpression(writer, param.getType());
			return;
		}
		
		// Write an array with one element
		IType type = param.getType().getElementType();
		writer.writeLDC(1);
		writer.writeNewArray(type, 1);
		writer.writeInsn(Opcodes.DUP);
		writer.writeLDC(0);
		this.value.writeExpression(writer, type);
		writer.writeInsn(type.getArrayStoreOpcode());
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return this.value == null ? EmptyIterator.instance : new SingletonIterator<IValue>(this.value);
	}

	@Override
	public boolean isResolved()
	{
		return this.value == null || this.value.isResolved();
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
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value == null)
		{
			return;
		}
		
		if (Formatting.getBoolean("method.call.java_format"))
		{
			Formatting.appendSeparator(buffer, "parameters.open_paren", '(');
			this.value.toString(prefix, buffer);
			Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
			return;
		}
		buffer.append(' ');
		this.value.toString(prefix, buffer);
	}
	
	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		if (this.value != null)
		{
			this.value.getType().toString("", buffer);
		}
		else
		{
			buffer.append("unknown");
		}
		buffer.append(')');
	}
}
