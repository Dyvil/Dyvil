package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.*;
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
			final ApplyMethodCall applyCall = (ApplyMethodCall) receiver;

			// x(y...) op= z
			// -> x(y...) = x(y...).op(z)
			// -> x.update(y..., x.apply(y...).op(z))

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue applyReceiver = helper.processValue(applyCall.getReceiver());
			final IArguments applyArguments = helper.processArguments(applyCall.getArguments());

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			applyCall.setArguments(applyArguments);
			applyCall.setReceiver(applyReceiver);
			final IValue update = new UpdateMethodCall(position, applyReceiver, applyArguments, op)
				                      .resolveCall(markers, context);

			return helper.finish(update);
		}
		else if (type == IValue.SUBSCRIPT_GET)
		{
			final SubscriptAccess subscriptAccess = (SubscriptAccess) receiver;

			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue subscriptReceiver = helper.processValue(subscriptAccess.getReceiver());
			final IArguments subscriptArguments = helper.processArguments(subscriptAccess.getArguments());

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			subscriptAccess.setReceiver(subscriptReceiver);
			subscriptAccess.setArguments(subscriptArguments);
			final IValue subscript = new SubscriptAssignment(position, subscriptReceiver, subscriptArguments, op)
				                         .resolveCall(markers, context);

			return helper.finish(subscript);
		}
		else if (type == IValue.FIELD_ACCESS)
		{
			final FieldAccess fieldAccess = (FieldAccess) receiver;

			final IncOperator incOp = getIncOperator(name, arguments, fieldAccess);
			if (incOp != null)
			{
				incOp.setPosition(position);
				return incOp.resolveOperator(markers, context);
			}

			// x op= z
			// -> x = x.op(z)

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue fieldReceiver = helper.processValue(fieldAccess.getReceiver());

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			fieldAccess.setReceiver(fieldReceiver);
			final FieldAssignment assignment = new FieldAssignment(position, fieldReceiver, fieldAccess.getField(), op);
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
