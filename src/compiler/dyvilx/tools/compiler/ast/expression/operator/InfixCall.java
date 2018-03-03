package dyvilx.tools.compiler.ast.expression.operator;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.transform.SideEffectHelper;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

public class InfixCall extends MethodCall
{
	public InfixCall(SourcePosition position, IValue lhs, Name name, IValue rhs)
	{
		super(position, lhs, name, new ArgumentList(rhs));
	}

	public InfixCall(SourcePosition position, IValue receiver, Name name, ArgumentList arguments)
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

	protected static IValue resolveCompound(MarkerList markers, IContext context, SourcePosition position, IValue lhs,
		                                       Name name, ArgumentList arguments)
	{
		final IValue op = new InfixCall(position, lhs, name, arguments).resolveCall(markers, context, false);
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
