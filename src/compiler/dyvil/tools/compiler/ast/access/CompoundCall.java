package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.intrinsic.IncOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CompoundCall
{
	private CompoundCall()
	{
		// no instances
	}

	protected static IValue resolveCall(MarkerList markers, IContext context, ICodePosition position, IValue receiver, Name name, IArguments arguments)
	{
		if (!receiver.isResolved())
		{
			return null;
		}
		if (arguments.isEmpty())
		{
			return null;
		}

		int type = receiver.valueTag();
		if (type == IValue.APPLY_CALL)
		{
			ApplyMethodCall applyCall = (ApplyMethodCall) receiver;

			// x(y...) op= z
			// -> x(y...) = x(y...).op(z)
			// -> x.update(y..., x.apply(y...).op(z))

			SideEffectHelper helper = new SideEffectHelper();

			IValue applyReceiver = applyCall.receiver = helper.processValue(applyCall.receiver);
			IArguments applyArguments = applyCall.arguments = helper.processArguments(applyCall.arguments);

			IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			IValue update = new UpdateMethodCall(position, applyReceiver, applyArguments, op)
				                .resolveCall(markers, context);

			return helper.finish(update);
		}
		else if (type == IValue.SUBSCRIPT_GET)
		{
			SubscriptAccess subscriptAccess = (SubscriptAccess) receiver;

			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))

			SideEffectHelper helper = new SideEffectHelper();

			IValue subscriptReceiver = subscriptAccess.receiver = helper.processValue(subscriptAccess.receiver);
			IArguments subscriptArguments = subscriptAccess.arguments = helper.processArguments(
				subscriptAccess.arguments);

			IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			IValue subscript = new SubscriptAssignment(position, subscriptReceiver, subscriptArguments, op)
				                   .resolveCall(markers, context);

			return helper.finish(subscript);
		}
		else if (type == IValue.FIELD_ACCESS)
		{
			final FieldAccess fieldAccess = (FieldAccess) receiver;

			final IncOperator op = getIncOperator(name, arguments, fieldAccess);
			if (op != null)
			{
				op.setPosition(position);
				return op.resolveOperator(markers, context);
			}

			// x op= z
			// -> x = x.op(z)

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue fieldReceiver = fieldAccess.receiver = helper.processValue(fieldAccess.receiver);

			final IValue methodCall = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (methodCall == null)
			{
				return null;
			}

			final FieldAssignment assignment = new FieldAssignment(position, fieldReceiver, fieldAccess.field,
			                                                       methodCall);
			return helper.finish(assignment);
		}

		return null;
	}

	private static IncOperator getIncOperator(Name name, IArguments arguments, FieldAccess fieldAccess)
	{
		if ((name == Names.plus || name == Names.minus) && IncOperator.isIncConvertible(fieldAccess.getType()))
		{
			final IValue value = arguments.getLastValue();
			if (value.valueTag() == IValue.INT)
			{
				int intValue = value.intValue();
				if (name == Names.minus)
				{
					intValue = -intValue;
				}
				return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), intValue, true);
			}
		}
		return null;
	}
}
