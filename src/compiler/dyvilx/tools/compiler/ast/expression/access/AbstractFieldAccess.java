package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.optional.OptionalChainAware;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.member.Named;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class AbstractFieldAccess implements IValue, Named, IReceiverAccess, OptionalChainAware
{
	// =============== Fields ===============

	// --------------- Source ---------------

	protected IValue receiver;
	protected Name   name;

	// --------------- Metadata ---------------

	protected SourcePosition position;
	protected IDataMember    field;
	protected IType          type;

	// =============== Constructors ===============

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

	public AbstractFieldAccess(SourcePosition position, IValue receiver, Name name)
	{
		this.position = position;
		this.receiver = receiver;
		this.name = name;
	}

	// =============== Properties ===============

	// --------------- Position ---------------

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

	// --------------- Receiver ---------------

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

	// --------------- Name ---------------

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

	// --------------- Name ---------------

	public IDataMember getField()
	{
		return this.field;
	}

	// --------------- Type ---------------

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
	public void setType(IType type)
	{
		this.type = type;
	}

	// =============== Methods ===============

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
	}

	// make sure to consider Optional Chain Awareness when overriding this method
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveReceiver(markers, context);

		if (this.field != null)
		{
			this.capture(markers, context); // to deal with capture correctly
			return OptionalChainAware.transform(this);
		}

		final IValue v = this.resolveAccess(markers, context);
		if (v != null)
		{
			// resolveAccess is Optional Chain Aware
			return v;
		}

		// Don't report an error if the receiver is not resolved
		if (this.receiver == null || this.receiver.isResolved())
		{
			this.reportResolve(markers);
		}

		return OptionalChainAware.transform(this);
	}

	@Override
	public void resolveReceiver(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
	}

	protected IValue resolveAccess(MarkerList markers, IContext context)
	{
		IValue access = this.resolveAsFieldOrMethod(markers, context);
		if (access != null)
		{
			return OptionalChainAware.transform(access);
		}

		// Qualified Type Name Resolution

		return this.resolveAsType(context);
	}

	private IValue resolveAsFieldOrMethod(MarkerList markers, IContext context)
	{
		IValue value;

		if (ICall.privateAccess(context, this.receiver))
		{
			IValue implicitValue;
			if (this.receiver == null && (implicitValue = context.resolveImplicit(null)) != null)
			{
				implicitValue = implicitValue.resolve(markers, context);

				value = this.resolveAsMethod(implicitValue, markers, context);
				if (value != null)
				{
					return value;
				}

				value = this.resolveAsField(implicitValue, markers, context);
				if (value != null)
				{
					return value;
				}
			}

			value = this.resolveAsField(this.receiver, markers, context);
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

			value = this.resolveAsField(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}
		}
		return null;
	}

	protected abstract IValue resolveAsField(IValue receiver, MarkerList markers, IContext context);

	protected abstract IValue resolveAsMethod(IValue receiver, MarkerList markers, IContext context);

	protected abstract IValue resolveAsType(IContext context);

	protected abstract void reportResolve(MarkerList markers);

	protected abstract void capture(MarkerList markers, IContext context);

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		if (this.field != null)
		{
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}

		if (this.field != null && !this.getType().isResolved())
		{
			markers.add(Markers.semanticError(this.position, "field.access.unresolved_type", this.name));
		}
	}

	// --------------- Formatting ---------------

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
