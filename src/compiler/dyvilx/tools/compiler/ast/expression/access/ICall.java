package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.function.Supplier;

public interface ICall extends IValue, WildcardLambdaAware
{
	default IValue getReceiver()
	{
		return null;
	}

	default void setReceiver(IValue receiver)
	{
	}

	void setArguments(ArgumentList arguments);

	ArgumentList getArguments();

	@Override
	default boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	default IValue resolve(MarkerList markers, IContext context)
	{
		final IValue wildcardLambda = WildcardLambdaAware.transform(this);
		if (wildcardLambda != null)
		{
			return wildcardLambda.resolve(markers, context);
		}

		this.resolveReceiver(markers, context);
		this.resolveArguments(markers, context);

		// Don't resolve and report an error if the receiver is not resolved
		IValue receiver = this.getReceiver();
		if (receiver != null && !receiver.isResolved())
		{
			return this;
		}

		// Don't resolve and report an error if the arguments are not resolved
		if (!this.getArguments().isResolved())
		{
			return this;
		}

		return this.resolveCall(markers, context, true);
	}

	@Override
	default int wildcardCount()
	{
		int count = 0;

		IValue receiver = this.getReceiver();
		if (receiver != null && receiver.isPartialWildcard())
		{
			count = 1;
		}

		for (IValue value : this.getArguments())
		{
			if (value.isPartialWildcard())
			{
				count++;
			}
		}

		return count;
	}

	@Override
	default IValue replaceWildcards(Supplier<IParameter> nextParameter)
	{
		final IValue receiver = this.getReceiver();
		if (receiver != null && receiver.isPartialWildcard())
		{
			this.setReceiver(receiver.withLambdaParameter(nextParameter.get()));
		}

		final ArgumentList arguments = this.getArguments();
		for (int i = 0, size = arguments.size(); i < size; i++)
		{
			final IValue argument = arguments.get(i);
			if (argument.isPartialWildcard())
			{
				arguments.set(i, argument.withLambdaParameter(nextParameter.get()));
			}
		}

		return this;
	}

	default void resolveReceiver(MarkerList markers, IContext context)
	{
	}

	void resolveArguments(MarkerList markers, IContext context);

	IValue resolveCall(MarkerList markers, IContext context, boolean report);

	static boolean privateAccess(IContext context, IValue receiver)
	{
		return receiver == null || context.getThisClass() == receiver.getType().getTheClass();
	}

	static IDataMember resolveField(IContext context, Typed receiver, Name name)
	{
		if (receiver != null)
		{
			return receiver.getType().resolveField(name);
		}

		final IDataMember match = context.resolveField(name);
		if (match != null)
		{
			return match;
		}

		return Types.BASE_CONTEXT.resolveField(name);
	}

	static IMethod resolveMethod(IContext context, IValue receiver, Name name, ArgumentList arguments)
	{
		return resolveMethods(context, receiver, name, arguments).getBestMember();
	}

	static MatchList<IMethod> resolveMethods(IContext context, IValue receiver, Name name, ArgumentList arguments)
	{
		@SuppressWarnings("UnnecessaryLocalVariable") final IImplicitContext implicitContext = context;

		final MatchList<IMethod> matches = new MatchList<>(implicitContext);

		// Methods available via the receiver type
		if (receiver != null)
		{
			receiver.getType().getMethodMatches(matches, receiver, name, arguments);
			if (matches.hasCandidate())
			{
				return matches;
			}
		}

		// Methods available via the first argument
		if (arguments.size() == 1)
		{
			arguments.getFirst().getType().getMethodMatches(matches, receiver, name, arguments);

			// Methods from the first argument have to be resolved before those from the enclosing context,
			// otherwise we would have strange behaviours e.g. with the expression -1 in a context where there is
			// a) a "prefix func -(_: SomeType)" and
			// b) an implicit conversion function from int to SomeType
			if (matches.hasCandidate())
			{
				return matches;
			}
		}

		// Methods available in the enclosing context
		context.getMethodMatches(matches, receiver, name, arguments);
		if (matches.hasCandidate())
		{
			return matches;
		}

		// Methods available through implicit conversions of the receiver
		if (receiver != null)
		{
			MatchList<IMethod> implicits = IContext.resolveImplicits(implicitContext, receiver, null);
			for (Candidate<IMethod> candidate : implicits)
			{
				candidate.getMember().getType().getMethodMatches(matches, receiver, name, arguments);
			}
			// This part is not in the loop because we do not want to depend on the ranking of the implicit conversion
			// methods. If two methods have the same ranking, the problem will propagate until later when they are
			// resolved again.
			if (matches.hasCandidate())
			{
				return matches;
			}
		}

		// Methods available through the Lang Header
		Types.BASE_CONTEXT.getMethodMatches(matches, receiver, name, arguments);
		return matches;
	}
}
