package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class FieldAssignment implements IValue, INamed, IReceiverAccess, IValueConsumer
{
	protected IValue receiver;
	protected Name   name;
	protected IValue value;

	// Metadata
	protected ICodePosition position;

	protected IDataMember field;
	protected IType       type;

	public FieldAssignment(ICodePosition position)
	{
		this.position = position;
	}

	public FieldAssignment(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}

	public FieldAssignment(ICodePosition position, IValue instance, IDataMember field, IValue value)
	{
		this.position = position;
		this.receiver = instance;
		this.field = field;
		this.value = value;

		if (field != null)
		{
			this.name = field.getName();
		}
	}

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
	public int valueTag()
	{
		return FIELD_ASSIGN;
	}

	@Override
	public boolean isPrimitive()
	{
		return this.field != null && this.field.getType().isPrimitive();
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return this.field != null;
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.field == null)
			{
				return Types.UNKNOWN;
			}

			final ITypeContext typeContext = this.receiver == null ? ITypeContext.NULL : this.receiver.getType();
			return this.type = this.field.getType().getConcreteType(typeContext).asReturnType();
		}
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSameType(type, Types.VOID))
		{
			return this;
		}

		final IValue typedValue = this.value.withType(type, typeContext, markers, context);
		if (typedValue == null)
		{
			return null;
		}

		this.value = typedValue;
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSameType(type, Types.VOID) || this.value.isType(type);
	}

	@Override
	public int getTypeMatch(IType type)
	{
		return this.value.getTypeMatch(type);
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setReceiver(IValue value)
	{
		this.value = value;
	}

	@Override
	public IValue getReceiver()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
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
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveReceiver(markers, context);

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}

		IValue v = this.resolveFieldAssignment(markers, context);
		if (v != null)
		{
			return v;
		}

		if (this.receiver != null && !this.receiver.isResolved())
		{
			return this;
		}

		Marker marker = Markers.semantic(this.position, "resolve.field", this.name.unqualified);
		if (this.receiver != null)
		{
			marker.addInfo(Markers.getSemantic("receiver.type", this.receiver.getType()));
		}

		markers.add(marker);
		return this;
	}

	protected IValue resolveFieldAssignment(MarkerList markers, IContext context)
	{
		if (ICall.privateAccess(context, this.receiver))
		{
			IValue value = this.resolveField(this.receiver, context);
			if (value != null)
			{
				return value;
			}

			// Duplicate in FieldAccess
			if (this.receiver == null)
			{
				final IValue implicit = context.getImplicit();
				if (implicit != null)
				{
					value = this.resolveField(implicit, context);
					if (value != null)
					{
						return value;
					}

					value = this.resolveMethod(implicit, markers, context);
					if (value != null)
					{
						return value;
					}
				}
			}

			value = this.resolveMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}
		}
		else
		{
			IValue value = this.resolveMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}
			value = this.resolveField(this.receiver, context);
			if (value != null)
			{
				return value;
			}
		}

		return null;
	}

	private IValue resolveField(IValue receiver, IContext context)
	{
		IDataMember field = ICall.resolveField(context, receiver, this.name);
		if (field != null)
		{
			this.field = field;
			this.receiver = receiver;
			return this;
		}
		return null;
	}

	private IValue resolveMethod(IValue receiver, MarkerList markers, IContext context)
	{
		final Name name = Util.addEq(this.name);

		final IArguments argument = new SingleArgument(this.value);
		final IMethod method = ICall.resolveMethod(context, receiver, name, argument);

		if (method == null)
		{
			return null;
		}

		final MethodAssignment methodAssignment = new MethodAssignment(this.position, receiver, method, argument);
		methodAssignment.checkArguments(markers, context);
		return methodAssignment;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
			this.value = this.field.checkAssign(markers, context, this.position, this.receiver, this.value);
		}

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(context, compilableList);
		}
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int lineNumber = this.getLineNumber();
		if (Types.isSameType(type, Types.VOID))
		{
			this.field.writeSet(writer, this.receiver, this.value, lineNumber);
			return;
		}

		final IType fieldType = this.getType();
		if (type == null)
		{
			type = fieldType;
		}

		if (this.receiver != null)
		{
			this.receiver.writeExpression(writer, null);
		}

		this.field.writeSet_PreValue(writer, lineNumber);

		if (this.receiver == null)
		{
			final boolean tempVar = this.field.writeSet_PreValue(writer, lineNumber);

			this.value.writeExpression(writer, fieldType);

			writer.visitInsn(tempVar ? Opcodes.AUTO_DUP_X1 : Opcodes.AUTO_DUP);
		}
		else
		{
			this.field.writeSet_PreValue(writer, lineNumber);

			this.value.writeExpression(writer, fieldType);

			writer.visitInsn(Opcodes.AUTO_DUP_X1);
		}

		this.field.writeSet_Wrap(writer, lineNumber);
		this.field.writeSet_Set(writer, lineNumber);

		// Return value left on stack
		fieldType.writeCast(writer, type, lineNumber);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
			buffer.append('.');
		}

		buffer.append(this.name);

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(prefix, buffer);
		}
	}
}
