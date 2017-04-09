package dyvil.tools.compiler.ast.expression.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.MethodCall;
import dyvil.tools.compiler.ast.expression.intrinsic.IncOperator;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
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
		super(position, lhs, name, new ArgumentList(rhs));
	}

	public InfixCall(ICodePosition position, IValue receiver, Name name, ArgumentList arguments)
	{
		super(position, receiver, name, arguments);
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		// Normal Method Resolution
		final MatchList<IMethod> candidates = this.resolveCandidates(context);
		if (candidates.hasCandidate())
		{
			return this.checkArguments(markers, context, candidates.getBestMember());
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
			this.reportResolve(markers, candidates);
			return this;
		}
		return null;
	}

	protected static IValue resolveCompound(MarkerList markers, IContext context, ICodePosition position, IValue lhs,
		                                       Name name, ArgumentList arguments)
	{
		IValue op = getIncOperator(name, lhs, arguments.getLast());
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

		return IncOperator.apply(lhs, rhs.intValue(), true);
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
			this.arguments.getFirst().toString(prefix, buffer);
		}
	}
}
