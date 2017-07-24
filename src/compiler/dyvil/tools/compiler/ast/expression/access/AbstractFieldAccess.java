package dyvil.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public abstract class AbstractFieldAccess implements IValue, INamed, IReceiverAccess
{
	protected IValue receiver;
	protected Name   name;

	// Metadata
	protected SourcePosition position;
	protected IDataMember    field;
	protected IType          type;

	public AbstractFieldAccess()
	{
	}

	public AbstractFieldAccess(IDataMember field)
	{
		if (field != null)
		{
			this.field = field;
			this.name = field.getName();
		}
	}

	public AbstractFieldAccess(SourcePosition position, IValue receiver, IDataMember field)
	{
		this(field);

		this.position = position;
		this.receiver = receiver;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IValue getReceiver()
	{
		return this.receiver;
	}

	@Override
	public void setReceiver(IValue receiver)
	{
		this.receiver = receiver;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	public IDataMember getField()
	{
		return this.field;
	}

	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}
		if (this.field == null)
		{
			return Types.UNKNOWN;
		}
		if (this.receiver == null)
		{
			return this.field.getType();
		}

		return this.type = this.field.getType().getConcreteType(this.receiver.getType());
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

		if (this.field != null)
		{
			return this;
		}

		final IValue v = this.resolveAccess(markers, context);
		if (v != null)
		{
			return v;
		}

		// Don't report an error if the receiver is not resolved
		if (this.receiver != null && !this.receiver.isResolved())
		{
			return this;
		}

		this.reportResolve(markers);
		return this;
	}

	protected abstract void reportResolve(MarkerList markers);

	protected IValue resolveAccess(MarkerList markers, IContext context)
	{
		IValue value;

		if (ICall.privateAccess(context, this.receiver))
		{
			final IValue implicit;
			if (this.receiver == null && (implicit = context.resolveImplicit(null)) != null)
			{
				value = this.resolveAsMethod(implicit, markers, context);
				if (value != null)
				{
					return value;
				}

				value = this.resolveAsField(implicit, context);
				if (value != null)
				{
					return value;
				}
			}

			value = this.resolveAsField(this.receiver, context);
			if (value != null)
			{
				return value;
			}

			value = this.resolveAsMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}
		}
		else
		{
			value = this.resolveAsMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}

			value = this.resolveAsField(this.receiver, context);
			if (value != null)
			{
				return value;
			}
		}

		// Qualified Type Name Resolution

		return this.resolveAsType(context);
	}

	protected abstract IValue resolveAsField(IValue receiver, IContext context);

	protected abstract IValue resolveAsMethod(IValue receiver, MarkerList markers, IContext context);

	protected abstract IValue resolveAsType(IContext context);

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(indent, buffer);
			buffer.append('.');
		}

		buffer.append(this.name);
	}
}
