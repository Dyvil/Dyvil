package dyvilx.tools.compiler.ast.expression.operator;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class PrefixCall extends MethodCall
{
	public PrefixCall(SourcePosition position, Name name)
	{
		super(position, null, name);
	}

	public PrefixCall(SourcePosition position, Name name, IValue argument)
	{
		super(position, null, name, new ArgumentList(argument));
	}

	@Override
	public int valueTag()
	{
		return PREFIX_CALL;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		final IOperator operator = IContext.resolveOperator(context, this.name, IOperator.PREFIX);
		if (operator == null)
		{
			markers.add(Markers.semantic(this.position, "operator.unresolved", this.name));
		}
		else if (operator.getType() == IOperator.CIRCUMFIX)
		{
			final IValue argument = this.arguments.getFirst();
			final Name name2 = operator.getName2();
			if (argument instanceof PostfixCall && ((PostfixCall) argument).getName() == name2)
			{
				this.arguments.setFirst(((PostfixCall) argument).getReceiver());
				this.name = Name.from(this.name.unqualified + "_" + name2.unqualified,
				                      this.name.qualified + "_" + name2.qualified);

				if (this.position != null)
				{
					this.position = this.position.to(argument.getPosition());
				}
			}
			else
			{
				OperatorElement.checkPosition(markers, this.position, operator, IOperator.PREFIX);
			}
		}
		else
		{
			OperatorElement.checkPosition(markers, this.position, operator, IOperator.PREFIX);
		}

		super.resolveTypes(markers, context);
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

		// Implicit Resolution
		final IValue implicitCall = this.resolveImplicitCall(markers, context);
		if (implicitCall != null)
		{
			return implicitCall;
		}

		// No apply Resolution
		if (report)
		{
			this.reportResolve(markers, candidates);
			return this;
		}
		return null;
	}

	@Override
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		final Name name = Name.from(this.name.unqualified + "_=", this.name.qualified + "_$eq");
		return new MethodCall(this.position, this.arguments.getFirst(), name, new ArgumentList(rhs));
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (!this.arguments.isEmpty())
		{
			this.arguments.getFirst().toString(prefix, buffer);
		}
	}
}
