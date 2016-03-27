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
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public final class SingleArgument implements IArguments, IValueConsumer
{
	private IValue  value;
	private boolean varArgsArray;
	
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
			return this.value.getTypeMatch(param.getInternalType());
		}
		return param.getValue() != null ? DEFAULT_MATCH : 0;
	}
	
	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		int size = this.size();
		if (index == size)
		{
			return VARARGS_MATCH;
		}
		if (index > size)
		{
			return 0;
		}
		
		float valueMatch = this.value.getTypeMatch(param.getInternalType());
		if (valueMatch > 0)
		{
			return valueMatch;
		}

		valueMatch = this.value.getTypeMatch(param.getInternalType().getElementType());
		return valueMatch > 0 ? valueMatch + VARARGS_MATCH : 0;
	}
	
	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		
		IType type = param.getInternalType();
		this.value = TypeChecker.convertValue(this.value, type, typeContext, markers, context,
		                                      IArguments.argumentMarkerSupplier(param));
	}
	
	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		
		final IType arrayType = param.getInternalType();
		if (this.value.isType(arrayType.getConcreteType(typeContext)))
		{
			this.value = TypeChecker.convertValue(this.value, arrayType, typeContext, markers, context,
			                                      IArguments.argumentMarkerSupplier(param));
			this.varArgsArray = true;
			return;
		}

		final IType elementType = arrayType.getElementType();
		this.value = TypeChecker.convertValue(this.value, elementType, typeContext, markers, context,
		                                      IArguments.argumentMarkerSupplier(param));
	}
	
	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index == 0 && this.value != null)
		{
			param.getInternalType().inferTypes(this.value.getType(), typeContext);
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
			param.getInternalType().inferTypes(type, typeContext);
			return;
		}
		
		param.getInternalType().getElementType().inferTypes(type, typeContext);
	}
	
	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index == 0 && this.value != null)
		{
			this.value.writeExpression(writer, param.getInternalType());
			return;
		}
		
		param.getValue().writeExpression(writer, param.getInternalType());
	}
	
	@Override
	public void writeVarargsValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index != 0 || this.value == null)
		{
			return;
		}
		if (this.varArgsArray)
		{
			// Write the value as is (it is an array)
			this.value.writeExpression(writer, param.getInternalType());
			return;
		}
		
		// Write an array with one element
		IType type = param.getInternalType().getElementType();
		writer.visitLdcInsn(1);
		writer.visitMultiANewArrayInsn(type, 1);
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(0);
		this.value.writeExpression(writer, type);
		writer.visitInsn(type.getArrayStoreOpcode());
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return this.value == null ? EmptyIterator.instance : new SingletonIterator<>(this.value);
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
