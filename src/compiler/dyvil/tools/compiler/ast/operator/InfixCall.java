package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.intrinsic.IncOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InfixCall extends MethodCall
{
	public InfixCall(ICodePosition position, IValue lhs, Name name, IValue rhs)
	{
		super(position, lhs, name, new SingleArgument(rhs));
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		IValue op = Operators.getInfix_Priority(this.receiver, this.name, this.arguments.getFirstValue());
		if (op != null)
		{
			// Intrinsic Infix Operators (namely ==, ===, != and !== for null)
			op.setPosition(this.position);
			return op.resolveOperator(markers, context);
		}

		// Normal Method Resolution
		if (this.resolveMethodCall(markers, context))
		{
			return this;
		}

		// Infix Operators
		op = Operators.getInfix(this.receiver, this.name, this.arguments.getFirstValue());
		if (op != null)
		{
			op.setPosition(this.position);
			return op.resolveOperator(markers, context);
		}

		// Compound Operators
		if (Util.hasEq(this.name))
		{
			final IValue compoundCall = resolveCompound(markers, context, this.position, this.receiver,
			                                            Util.removeEq(this.name), this.arguments);
			if (compoundCall != null)
			{
				return compoundCall;
			}
		}

		// No Implicit or Apply Resolution
		if (report)
		{
			this.reportResolve(markers, context);
			return this;
		}
		return null;
	}

	protected static IValue resolveCompound(MarkerList markers, IContext context, ICodePosition position, IValue receiver, Name name, IArguments arguments)
	{
		int type = receiver.valueTag();
		if (type == APPLY_CALL)
		{
			final ApplyMethodCall applyCall = (ApplyMethodCall) receiver;

			// x(y...) op= z
			// -> x(y...) = x(y...).op(z)
			// -> x.update(y..., x.apply(y...).op(z))

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue applyReceiver = helper.processValue(applyCall.getReceiver());
			final IArguments applyArguments = helper.processArguments(applyCall.getArguments());

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context, false);
			if (op == null)
			{
				return null;
			}

			applyCall.setArguments(applyArguments);
			applyCall.setReceiver(applyReceiver);
			final IValue update = new UpdateMethodCall(position, applyReceiver, applyArguments, op)
				                      .resolveCall(markers, context, true);

			return helper.finish(update);
		}
		else if (type == SUBSCRIPT_GET)
		{
			final SubscriptAccess subscriptAccess = (SubscriptAccess) receiver;

			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))

			final SideEffectHelper helper = new SideEffectHelper();

			final IValue subscriptReceiver = helper.processValue(subscriptAccess.getReceiver());
			final IArguments subscriptArguments = helper.processArguments(subscriptAccess.getArguments());

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context, false);
			if (op == null)
			{
				return null;
			}

			subscriptAccess.setReceiver(subscriptReceiver);
			subscriptAccess.setArguments(subscriptArguments);
			final IValue subscript = new SubscriptAssignment(position, subscriptReceiver, subscriptArguments, op)
				                         .resolveCall(markers, context, true);

			return helper.finish(subscript);
		}
		else if (type == FIELD_ACCESS)
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

			final IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context, false);
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
		if ((name != Names.plus && name != Names.minus) || !IncOperator.isIncConvertible(fieldAccess.getType()))
		{
			return null;
		}

		final IValue value = arguments.getLastValue();
		if (value.valueTag() != INT)
		{
			return null;
		}

		int intValue = value.intValue();
		if (name == Names.minus)
		{
			intValue = -intValue;
		}
		return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), intValue, true);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		buffer.append(' ').append(this.name.unqualified).append(' ');
		if (!this.arguments.isEmpty())
		{
			this.arguments.getFirstValue().toString(prefix, buffer);
		}
	}
}
