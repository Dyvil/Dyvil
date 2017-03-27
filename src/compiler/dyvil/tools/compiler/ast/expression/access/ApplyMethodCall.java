package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.statement.Closure;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ApplyMethodCall extends AbstractCall
{
	public ApplyMethodCall(ICodePosition position)
	{
		this.position = position;
	}

	public ApplyMethodCall(ICodePosition position, IValue receiver)
	{
		this.position = position;
		this.receiver = receiver;
	}

	public ApplyMethodCall(ICodePosition position, IValue instance, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}

	public ApplyMethodCall(ICodePosition position, IValue instance, IMethod method, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
		this.method = method;
	}

	@Override
	public int valueTag()
	{
		return APPLY_CALL;
	}

	@Override
	public Name getName()
	{
		return Names.apply;
	}

	@Override
	protected Name getReferenceName()
	{
		return Names.apply_$amp;
	}

	@Override
	public IValue toAssignment(IValue rhs, ICodePosition position)
	{
		return new UpdateMethodCall(this.position.to(position), this.receiver, this.arguments, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, ICodePosition position, MarkerList markers, IContext context,
		                                  SideEffectHelper helper)
	{
		// x(y...) op= z
		// -> x(y...) = x(y...).op(z)
		// -> x.update(y..., x.apply(y...).op(z))

		final IValue applyReceiver = helper.processValue(this.receiver);
		final ArgumentList applyArguments = helper.processArguments(this.arguments);

		this.arguments = applyArguments;
		this.receiver = applyReceiver;

		return new UpdateMethodCall(position, applyReceiver, applyArguments, rhs).resolveCall(markers, context, true);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Merge Applied Statement Lists if a non-curried method version is
		// available. Doesn't works with multiple applied statement lists.
		// with(x...) { statements }
		// -> with(x..., { statements })
		if (this.arguments.size() == 1 && this.receiver instanceof ICall)
		{
			final ICall call = (ICall) this.receiver;
			IValue argument = this.arguments.getFirstValue();

			if (argument instanceof Closure)
			{
				argument = argument.resolve(markers, context);

				final ArgumentList oldArgs = call.getArguments();
				call.resolveReceiver(markers, context);
				call.resolveArguments(markers, context);

				call.setArguments(oldArgs.withLastValue(null, argument));

				final IValue resolvedCall = call.resolveCall(markers, context, false);
				if (resolvedCall != null)
				{
					return resolvedCall;
				}

				// Revert
				call.setArguments(oldArgs);

				this.receiver = call.resolveCall(markers, context, true);
				return this.resolveCall(markers, context, true);
			}
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

		if (this.genericData != null)
		{
			buffer.append(".apply");
			this.genericData.toString(prefix, buffer);
		}

		this.arguments.toString(prefix, buffer);
	}
}
