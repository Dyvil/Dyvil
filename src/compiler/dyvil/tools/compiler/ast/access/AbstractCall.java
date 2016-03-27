package dyvil.tools.compiler.ast.access;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class AbstractCall implements ICall, IReceiverAccess
{
	protected ICodePosition position;

	protected IValue receiver;
	protected IArguments arguments = EmptyArguments.INSTANCE;
	protected GenericData genericData;

	// Metadata
	protected IMethod method;
	protected IType   type;

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public void setReceiver(IValue value)
	{
		this.receiver = value;
	}

	@Override
	public IValue getReceiver()
	{
		return this.receiver;
	}

	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}

	public void setGenericData(GenericData data)
	{
		this.genericData = data;
	}

	public GenericData getGenericData()
	{
		if (this.genericData != null)
		{
			return this.genericData;
		}
		if (this.method == null)
		{
			return this.genericData = new GenericData();
		}
		return this.genericData = this.method.getGenericData(null, this.receiver, this.arguments);
	}

	@Override
	public boolean isResolved()
	{
		return this.method != null;
	}

	public IMethod getMethod()
	{
		return this.method;
	}

	@Override
	public boolean isPrimitive()
	{
		return this.method != null && (this.method.isIntrinsic() || this.getType().isPrimitive());
	}

	@Override
	public boolean hasSideEffects()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return Types.UNKNOWN;
		}
		if (this.type == null)
		{
			this.type = this.method.getType().getConcreteType(this.getGenericData()).asReturnType();
		}
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this.isType(type) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSameType(type, Types.VOID) || this.method != null && Types.isAssignable(this.getType(), type);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
		if (this.arguments.isEmpty())
		{
			this.arguments = EmptyArguments.VISIBLE;
		}
		else
		{
			this.arguments.resolveTypes(markers, context);
		}

		if (this.genericData != null)
		{
			this.genericData.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveReceiver(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
	}

	@Override
	public void resolveArguments(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
	}

	@Override
	public abstract IValue resolveCall(MarkerList markers, IContext context);

	@Override
	public void checkArguments(MarkerList markers, IContext context)
	{
		if (this.method != null)
		{
			GenericData data;
			if (this.genericData != null)
			{
				data = this.genericData = this.method.getGenericData(this.genericData, this.receiver, this.arguments);
			}
			else
			{
				data = this.getGenericData();
			}

			this.receiver = this.method
				                .checkArguments(markers, this.position, context, this.receiver, this.arguments, data);
		}

		this.type = null;
		this.type = this.getType();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}

		if (this.method != null)
		{
			this.method
				.checkCall(markers, this.position, context, this.receiver, this.arguments, this.getGenericData());
		}

		this.arguments.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(context, compilableList);
		}
		this.arguments.cleanup(context, compilableList);
		return this;
	}

	// Inlined for performance
	@Override
	public int getLineNumber()
	{
		if (this.position == null)
		{
			return 0;
		}
		return this.position.startLine();
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.getType();
		}

		this.method.writeCall(writer, this.receiver, this.arguments, this.genericData, type, this.getLineNumber());
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeJump(writer, dest, this.receiver, this.arguments, this.genericData, this.getLineNumber());
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeInvJump(writer, dest, this.receiver, this.arguments, this.genericData, this.getLineNumber());
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}
