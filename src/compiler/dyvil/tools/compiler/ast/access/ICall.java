package dyvil.tools.compiler.ast.access;

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
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
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
	void setArguments(IArguments arguments);

	IArguments getArguments();

	default int wildcardCount()
	{
		int count = 0;

		IValue receiver = this.getReceiver();
		if (receiver != null && receiver.valueTag() == IValue.WILDCARD)
		{
			count = 1;
		}

		for (IValue value : this.getArguments())
		{
			if (value.valueTag() == IValue.WILDCARD)
			{
				count++;
			}
		}

		return count;
	}

	default IValue toLambda(MarkerList markers, IContext context, int wildcards)
	{
		ICodePosition position = this.getPosition();

		IParameter[] parameters = new IParameter[wildcards];
		for (int i = 0; i < wildcards; i++)
		{
			parameters[i] = new CodeParameter(position, Name.getQualified("wildcard$" + i), Types.UNKNOWN,
			                                  EmptyModifiers.INSTANCE, null);
		}

		int index = 0;

		IValue receiver = this.getReceiver();
		if (receiver != null && receiver.valueTag() == IValue.WILDCARD)
		{
			this.setReceiver(convertWildcardValue(receiver, parameters[index++]));
		}

		IArguments arguments = this.getArguments();
		for (int i = 0, size = arguments.size(); i < size; i++)
		{
			IValue value = arguments.getValue(i, null);
			if (value.valueTag() == IValue.WILDCARD)
			{
				arguments.setValue(i, null, convertWildcardValue(value, parameters[index++]));
			}
		}

		LambdaExpr lambdaExpr = new LambdaExpr(position, parameters, wildcards);
		lambdaExpr.setImplicitParameters(true);
		lambdaExpr.setValue(this);
		return lambdaExpr.resolve(markers, context);
	}

	static IValue convertWildcardValue(IValue value, IParameter parameter)
	{
		ICodePosition valuePosition = value.getPosition();
		parameter.setPosition(valuePosition);
		return new FieldAccess(valuePosition, null, parameter);
	}

	@Override
	default boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	default IValue resolve(MarkerList markers, IContext context)
	{
		// Wildcard Conversion
		int wildcards = this.wildcardCount();
		if (wildcards > 0)
		{
			return this.toLambda(markers, context, wildcards);
		}

		this.resolveReceiver(markers, context);
		this.resolveArguments(markers, context);

		IValue resolved = this.resolveCall(markers, context);
		if (resolved != null)
		{
			return resolved;
		}

		// Don't report an error if the receiver is not resolved
		IValue receiver = this.getReceiver();
		if (receiver != null && !receiver.isResolved())
		{
			return this;
		}

		// Don't report an error if the arguments are not resolved
		if (!this.getArguments().isResolved())
		{
			return this;
		}

		this.reportResolve(markers, context);
		return this;
	}

	void checkArguments(MarkerList markers, IContext context);

	IValue resolveCall(MarkerList markers, IContext context);

	default void resolveReceiver(MarkerList markers, IContext context)
	{
	}

	void resolveArguments(MarkerList markers, IContext context);

	void reportResolve(MarkerList markers, IContext context);

	static void addResolveMarker(MarkerList markers, ICodePosition position, IValue receiver, Name name, IArguments arguments)
	{
		if (arguments == EmptyArguments.INSTANCE)
		{
			Marker marker = Markers.semantic(position, "resolve.method_field", name);
			if (receiver != null)
			{
				marker.addInfo(Markers.getSemantic("receiver.type", receiver.getType()));
			}

			markers.add(marker);
			return;
		}

		Marker marker = Markers.semantic(position, "resolve.method", name);
		if (receiver != null)
		{
			marker.addInfo(Markers.getSemantic("receiver.type", receiver.getType()));
		}
		if (!arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}

		markers.add(marker);
	}

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

		return Types.LANG_HEADER.resolveField(name);
	}

	static IMethod resolveMethod(IContext context, IValue receiver, Name name, IArguments arguments)
	{
		return resolveMethods(context, receiver, name, arguments).getBestMember();
	}

	static MatchList<IMethod> resolveMethods(IContext context, IValue receiver, Name name, IArguments arguments)
	{
		@SuppressWarnings("UnnecessaryLocalVariable") final IImplicitContext implicitContext = context;

		final MatchList<IMethod> matches = new MatchList<>(implicitContext);

		// Methods available through the receiver
		if (receiver != null)
		{
			receiver.getType().getMethodMatches(matches, receiver, name, arguments);
			if (!matches.isEmpty())
			{
				return matches;
			}
		}

		// Methods available through the first argument
		if (arguments.size() == 1)
		{
			arguments.getFirstValue().getType().getMethodMatches(matches, receiver, name, arguments);
			if (!matches.isEmpty())
			{
				return matches;
			}
		}

		// Methods available in the current context
		context.getMethodMatches(matches, receiver, name, arguments);
		if (!matches.isEmpty())
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
				if (!matches.isEmpty())
				{
					return matches;
				}
			}
		}

		// Methods available through the Lang Header
		Types.LANG_HEADER.getMethodMatches(matches, receiver, name, arguments);
		return matches;
	}
}
