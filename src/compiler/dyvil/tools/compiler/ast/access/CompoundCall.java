package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.operator.IncOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.transform.Names;
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
			
			IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			return new UpdateMethodCall(position, applyCall.receiver,
			                            applyCall.arguments.withLastValue(Names.update, op))
					.resolveCall(markers, context);
		}
		else if (type == IValue.SUBSCRIPT_GET)
		{
			SubscriptGetter subscriptGetter = (SubscriptGetter) receiver;
			
			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))
			
			IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			return new SubscriptSetter(position, subscriptGetter.receiver,
			                           subscriptGetter.arguments.withLastValue(Names.subscript_$eq, op))
					.resolveCall(markers, context);
		}
		else if (type == IValue.FIELD_ACCESS)
		{
			FieldAccess fieldAccess = (FieldAccess) receiver;

			if (isIncConvertible(fieldAccess.getType(), name))
			{
				IValue value = arguments.getLastValue();
				if (IValue.isNumeric(value.valueTag()))
				{
					int intValue = value.intValue();
					if (name == Names.minus)
					{
						intValue = -intValue;
					}
					return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), intValue, false);
				}
			}

			// x op= z
			// -> x = x.op(z)

			IValue op = new MethodCall(position, receiver, name, arguments).resolveCall(markers, context);
			if (op == null)
			{
				return null;
			}

			return new FieldAssignment(position, fieldAccess.getReceiver(), fieldAccess.field, op);
		}

		return null;
	}

	private static boolean isIncConvertible(IType type, Name name)
	{
		if (name != Names.plus && name != Names.minus)
		{
			return false;
		}
		if (!type.isPrimitive())
		{
			return false;
		}
		switch (type.typeTag())
		{
		case PrimitiveType.BOOLEAN_CODE:
		case PrimitiveType.VOID_CODE:
			return false;
		}
		return true;
	}
}
