package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.EmptyIterator;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
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
	private IValue value;

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
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param, IImplicitContext implicitContext)
	{
		if (argumentIndex != 0 || this.value == null)
		{
			return param.isVarargs() ? 0 : -1;
		}

		if (!param.isVarargs() || this.value.checkVarargs(false))
		{
			return ArgumentList.checkMatch(values, types, matchStartIndex + argumentIndex, this.value, param.getInternalType(),
			                               implicitContext) ? 0 : -1;
		}

		final IType elementType = param.getInternalType().getElementType();
		if (ArgumentList.checkMatch(values, types, matchStartIndex + argumentIndex, this.value, elementType, implicitContext))
		{
			// One argument applied as varargs
			return 1;
		}
		return -1;
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index != 0 || this.value == null)
		{
			return;
		}

		if (param.isVarargs())
		{
			this.checkVarargsValue(param, typeContext, markers, context);
			return;
		}

		IType type = param.getInternalType();
		this.value = TypeChecker.convertValue(this.value, type, typeContext, markers, context,
		                                      IArguments.argumentMarkerSupplier(param));
	}

	private void checkVarargsValue(IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType arrayType = param.getInternalType();
		if (this.value.checkVarargs(true))
		{
			this.value = TypeChecker.convertValue(this.value, arrayType, typeContext, markers, context,
			                                      IArguments.argumentMarkerSupplier(param));
			return;
		}

		final IType elementType = arrayType.getElementType();
		this.value = TypeChecker.convertValue(this.value, elementType, typeContext, markers, context,
		                                      IArguments.argumentMarkerSupplier(param));

		this.value = new ArrayExpr(this.value.getPosition(), new IValue[] { this.value }, 1);
		this.value.setType(arrayType);
	}

	private void inferVarargsType(IParameter param, ITypeContext typeContext)
	{
		final IType type = this.value.getType();
		if (this.value.checkVarargs(true))
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

		EmptyArguments.writeArguments(writer, param);
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
	public IArguments copy()
	{
		return new SingleArgument(this.value);
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
