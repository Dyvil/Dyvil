package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.intrinsic.IncOperator;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
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

	public InfixCall(ICodePosition position, IValue receiver, Name name, IArguments arguments)
	{
		super(position, receiver, name, arguments);
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		// Normal Method Resolution
		final MatchList<IMethod> ambiguousCandidates = this.resolveMethodCall(markers, context);
		if (ambiguousCandidates == null)
		{
			return this;
		}

		// Infix Operators
		IValue op = Operators.getInfix(this.receiver, this.name, this.arguments.getFirstValue());
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
			this.reportResolve(markers, ambiguousCandidates);
			return this;
		}
		return null;
	}

	protected static IValue resolveCompound(MarkerList markers, IContext context, ICodePosition position, IValue lhs,
		                                       Name name, IArguments arguments)
	{
		IValue op = getIncOperator(name, lhs, arguments.getLastValue());
		if (op != null)
		{
			return op;
		}

		op = new InfixCall(position, lhs, name, arguments).resolveCall(markers, context, false);
		if (op == null)
		{
			return null;
		}

		final SideEffectHelper helper = new SideEffectHelper();
		final IValue assignment = lhs.toCompoundAssignment(op, position, markers, context, helper);
		if (assignment != null)
		{
			return helper.finish(assignment);
		}

		return null;
	}

	private static IncOperator getIncOperator(Name name, IValue lhs, IValue rhs)
	{
		// Operator has to be either + or -
		if (name != Names.plus && name != Names.minus)
		{
			return null;
		}

		// Right-hand operand must be of type int
		if (rhs.valueTag() != INT)
		{
			return null;
		}

		// Left-hand operand must be a field access and inc-convertible
		if (lhs.valueTag() != IValue.FIELD_ACCESS || !IncOperator.isIncConvertible(lhs.getType()))
		{
			return null;
		}

		final FieldAccess fieldAccess = (FieldAccess) lhs;
		int intValue = rhs.intValue();
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
			buffer.append(' ');
		}

		buffer.append(this.name.unqualified);

		if (!this.arguments.isEmpty())
		{
			buffer.append(' ');
			this.arguments.getFirstValue().toString(prefix, buffer);
		}
	}
}
