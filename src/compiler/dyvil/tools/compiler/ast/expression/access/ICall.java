package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface ICall extends IValue, IArgumentsConsumer
{
	default IValue getReceiver()
	{
		return null;
	}

	default void setReceiver(IValue receiver)
	{
	}

	@Override
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
		this.resolveReceiver(markers, context);
		this.resolveArguments(markers, context);

		final int wildcards = this.wildcardCount();
		if (wildcards > 0)
		{
			return this.toLambda(markers, context, wildcards);
		}

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

	default IValue toLambda(MarkerList markers, IContext context, int wildcards)
	{
		ICodePosition position = this.getPosition();

		final IParameter[] parameters = new IParameter[wildcards];
		for (int i = 0; i < wildcards; i++)
		{
			parameters[i] = new CodeParameter(null, position, Name.fromRaw("wildcard$" + i), Types.UNKNOWN,
			                                  EmptyModifiers.INSTANCE, null);
		}

		int parIndex = 0;

		final IValue receiver = this.getReceiver();
		if (receiver != null && receiver.isPartialWildcard())
		{
			this.setReceiver(receiver.withLambdaParameter(parameters[parIndex++]));
		}

		final ArgumentList arguments = this.getArguments();
		for (int i = 0, size = arguments.size(); i < size; i++)
		{
			final IValue argument = arguments.getValue(i, null);
			if (argument.isPartialWildcard())
			{
				arguments.set(i, null, argument.withLambdaParameter(parameters[parIndex++]));
			}
		}

		final LambdaExpr lambdaExpr = new LambdaExpr(position, parameters, wildcards);
		lambdaExpr.setImplicitParameters(true);
		lambdaExpr.setValue(this);
		return lambdaExpr.resolve(markers, context);
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

	static IDataMember resolveField(IContext context, ITyped receiver, Name name)
	{
		if (receiver != null)
		{
			final IType receiverType = receiver.getType();
			if (receiverType != null)
			{
				final IDataMember match = receiverType.resolveField(name);
				if (match != null)
				{
					return match;
				}
			}

			return null;
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

		// Methods available through the receiver
		if (receiver != null)
		{
			receiver.getType().getMethodMatches(matches, receiver, name, arguments);
			if (matches.hasCandidate())
			{
				return matches;
			}
		}

		// Methods available through the first argument
		if (arguments.size() == 1)
		{
			arguments.getFirstValue().getType().getMethodMatches(matches, receiver, name, arguments);
			if (matches.hasCandidate())
			{
				return matches;
			}
		}

		// Methods available in the current context
		context.getMethodMatches(matches, receiver, name, arguments);
		if (matches.hasCandidate())
		{
			return matches;
		}

		// Methods available through implicit conversions
		if (receiver != null)
		{
			MatchList<IMethod> implicits = IContext.resolveImplicits(implicitContext, receiver, null);
			for (Candidate<IMethod> candidate : implicits)
			{
				candidate.getMember().getType().getMethodMatches(matches, receiver, name, arguments);
				if (matches.hasCandidate())
				{
					return matches;
				}
			}
		}

		// Methods available through the Lang Header
		Types.BASE_CONTEXT.getMethodMatches(matches, receiver, name, arguments);
		return matches;
	}
}
