package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class SubscriptAccess extends AbstractCall
{
	public SubscriptAccess(ICodePosition position)
	{
		this.position = position;
	}

	public SubscriptAccess(ICodePosition position, IValue instance)
	{
		this.position = position;
		this.receiver = instance;
	}

	public SubscriptAccess(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}

	@Override
	public int valueTag()
	{
		return SUBSCRIPT_GET;
	}

	@Override
	public Name getName()
	{
		return Names.subscript;
	}

	@Override
	protected Name getReferenceName()
	{
		return Names.subscript_$amp;
	}

	@Override
	public IValue toAssignment(IValue rhs, ICodePosition position)
	{
		return new SubscriptAssignment(this.position.to(position), this.receiver, this.arguments, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, ICodePosition position, MarkerList markers, IContext context,
		                                  SideEffectHelper helper)
	{
		// x[y...] op= z
		// -> x[y...] = x[y...].op(z)
		// -> x.subscript_=(y..., x.subscript(y...).op(z))

		final IValue subscriptReceiver = helper.processValue(this.receiver);
		final IArguments subscriptArguments = helper.processArguments(this.arguments);

		this.receiver = subscriptReceiver;
		this.arguments = subscriptArguments;
		return new SubscriptAssignment(position, subscriptReceiver, subscriptArguments, rhs)
			       .resolveCall(markers, context, true);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.receiver instanceof ICall)
		// false if receiver == null
		{
			ICall call = (ICall) this.receiver;

			// Resolve Receiver if necessary
			call.resolveReceiver(markers, context);
			call.resolveArguments(markers, context);

			IArguments oldArgs = call.getArguments();

			ArrayExpr array = new ArrayExpr(this.position, this.arguments.size());
			for (IValue v : this.arguments)
			{
				array.addValue(v);
			}

			call.setArguments(oldArgs.withLastValue(Names.subscript, array));

			IValue resolvedCall = call.resolveCall(markers, context, false);
			if (resolvedCall != null)
			{
				return resolvedCall;
			}

			// Revert
			call.setArguments(oldArgs);

			this.receiver = call.resolveCall(markers, context, true);
			return this.resolveCall(markers, context, true);
		}

		return super.resolve(markers, context);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}

		Formatting.appendSeparator(buffer, "method.subscript.open_bracket", '[');

		int count = this.arguments.size();
		if (count > 0)
		{
			this.arguments.getValue(0, null).toString(prefix, buffer);
			for (int i = 1; i < count; i++)
			{
				Formatting.appendSeparator(buffer, "method.subscript.separator", ',');
				this.arguments.getValue(i, null).toString(prefix, buffer);
			}
		}

		if (Formatting.getBoolean("method.subscript.close_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
