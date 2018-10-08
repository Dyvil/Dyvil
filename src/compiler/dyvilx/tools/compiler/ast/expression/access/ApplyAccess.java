package dyvilx.tools.compiler.ast.expression.access;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.expression.Closure;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.transform.SideEffectHelper;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ApplyAccess extends AbstractCall
{
	public ApplyAccess(SourcePosition position)
	{
		this.position = position;
	}

	public ApplyAccess(SourcePosition position, IValue receiver)
	{
		this.position = position;
		this.receiver = receiver;
	}

	public ApplyAccess(SourcePosition position, IValue instance, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}

	public ApplyAccess(SourcePosition position, IValue instance, IMethod method, ArgumentList arguments)
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
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return new ApplyAssignment(this.position.to(position), this.receiver, this.arguments, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, SourcePosition position, MarkerList markers, IContext context,
		                                  SideEffectHelper helper)
	{
		// x(y...) (op)= z
		// -> x(y...) = x(y...) (op) z
		// note that x and y... are each only evaluated once

		final IValue applyReceiver = helper.processValue(this.receiver);
		final ArgumentList applyArguments = helper.processArguments(this.arguments);

		this.arguments = applyArguments;
		this.receiver = applyReceiver;

		return new ApplyAssignment(position, applyReceiver, applyArguments, rhs).resolveCall(markers, context, true);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Merge trailing closures if a non-curried method version is
		// available. Doesn't works with multiple trailing closures.
		// with(x...) { statements }
		// -> with(x..., { statements })
		if (this.arguments.size() == 1 && this.receiver instanceof ICall)
		{
			final ICall call = (ICall) this.receiver;
			IValue argument = this.arguments.getFirst();

			if (Closure.isTrailingClosure(argument))
			{
				call.resolveReceiver(markers, context);
				call.resolveArguments(markers, context);

				final ArgumentList oldArgs = call.getArguments();

				call.setArguments(oldArgs.appended(null, argument));

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
