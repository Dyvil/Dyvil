package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;

import dyvil.collection.iterator.SingletonIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
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
		return index == 0 ? this.value.getType() : Types.UNKNOWN;
	}
	
	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		if (index == 0)
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
		if (index > 1) {
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
		if (index != 0)
		{
			return;
		}
		
		IType type = param.getActualType();
		IValue value1 = type.convertValue(this.value, typeContext, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(this.value.getPosition(), "method.access.argument_type", param.getName());
			marker.addInfo("Required Type: " + type);
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
		}
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0)
		{
			return;
		}
		
		IType type = param.getActualType();
		IValue value1 = type.convertValue(this.value, typeContext, markers, context);
		if (value1 != null)
		{
			this.value = value1;
			this.varargs = true;
			return;
		}
		
		value1 = type.getElementType().convertValue(this.value, typeContext, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(this.value.getPosition(), "method.access.argument_type", param.getName());
			marker.addInfo("Required Type: " + type);
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
		}
	}
	
	@Override
	public void writeValue(int index, Name name, IValue defaultValue, MethodWriter writer) throws BytecodeException
	{
		if (index == 0)
		{
			this.value.writeExpression(writer);
			return;
		}
		
		defaultValue.writeExpression(writer);
	}
	
	@Override
	public void writeVarargsValue(int index, Name name, IType type, MethodWriter writer) throws BytecodeException
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
		return new SingletonIterator<IValue>(this.value);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.value = this.value.cleanup(context, compilableList);
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
		if (this.value == null)
		{
			return;
		}
		
		if (Formatting.Method.useJavaFormat)
		{
			buffer.append('(');
			this.value.toString(prefix, buffer);
			buffer.append(')');
			return;
		}
		buffer.append(' ');
		this.value.toString(prefix, buffer);
	}
	
	@Override
	public void typesToString(StringBuilder buffer)
	{
		if (this.value != null)
		{
			this.value.getType().toString("", buffer);
		}
		else
		{
			buffer.append("unknown");
		}
	}
}
