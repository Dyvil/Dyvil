package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Collections;
import java.util.Iterator;

public final class EmptyArguments implements IArguments
{
	public static final EmptyArguments VISIBLE  = new EmptyArguments(true);
	public static final EmptyArguments INSTANCE = new EmptyArguments(false);

	private boolean visible;

	private EmptyArguments()
	{
	}

	private EmptyArguments(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public Iterator<IValue> iterator()
	{
		return Collections.emptyIterator();
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public IArguments dropFirstValue()
	{
		return null;
	}

	@Override
	public IArguments withLastValue(IValue value)
	{
		return new SingleArgument(value);
	}

	@Override
	public IValue getFirstValue()
	{
		return null;
	}

	@Override
	public void setFirstValue(IValue value)
	{
	}

	@Override
	public IValue getLastValue()
	{
		return null;
	}

	@Override
	public void setLastValue(IValue value)
	{
	}

	@Override
	public void setValue(int index, IParameter param, IValue value)
	{
	}

	@Override
	public IValue getValue(int index, IParameter param)
	{
		return null;
	}

	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
	}

	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		if (param.isVarargs())
		{
			return VARARGS_MATCH;
		}
		return param.getValue() != null ? DEFAULT_MATCH : 0;
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		writeArguments(writer, param);
	}

	protected static void writeArguments(MethodWriter writer, IParameter param)
	{
		if (param.isVarargs())
		{
			writer.visitLdcInsn(0);
			writer.visitMultiANewArrayInsn(param.getType().getElementType(), 1);
			return;
		}

		param.getValue().writeExpression(writer, param.getType());
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (!this.visible)
		{
			return;
		}

		if (Formatting.getBoolean("parameters.empty.space_between"))
		{
			buffer.append("( )");
		}
		else
		{
			buffer.append("()");
		}
	}

	@Override
	public void typesToString(StringBuilder buffer)
	{
	}
}
