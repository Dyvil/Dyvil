package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.optional.OptionalChainAware;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class AbstractCall implements ICall, IReceiverAccess, OptionalChainAware
{
	protected SourcePosition position;

	protected IValue       receiver;
	protected ArgumentList arguments;
	protected GenericData  genericData;

	// Metadata
	protected IMethod method;
	protected IType   type;

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IValue getReceiver()
	{
		return this.receiver;
	}

	@Override
	public void setReceiver(IValue receiver)
	{
		this.receiver = receiver;
	}

	public abstract Name getName();

	@Override
	public ArgumentList getArguments()
	{
		return this.arguments;
	}

	@Override
	public void setArguments(ArgumentList arguments)
	{
		this.arguments = arguments;
	}

	public GenericData getGenericData()
	{
		if (this.genericData != null)
		{
			return this.genericData;
		}
		if (this.method == null)
		{
			return this.genericData = new GenericData();
		}
		return this.genericData = this.method.getGenericData(null, this.receiver, this.arguments);
	}

	public void setGenericData(GenericData data)
	{
		this.genericData = data;
	}

	@Override
	public boolean isResolved()
	{
		return this.method != null;
	}

	public IMethod getMethod()
	{
		return this.method;
	}

	@Override
	public boolean hasSideEffects()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return Types.UNKNOWN;
		}
		if (this.type == null)
		{
			this.type = this.method.getType().getConcreteType(this.getGenericData());
		}
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this.isType(type) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isVoid(type) || this.method != null && Types.isSuperType(type, this.getType());
	}

	protected abstract Name getReferenceName();

	@Override
	public IValue toReferenceValue(MarkerList markers, IContext context)
	{
		final MethodCall methodCall = new MethodCall(this.position, this.receiver, this.getReferenceName(),
		                                             this.arguments);
		methodCall.setGenericData(this.genericData);
		return methodCall.resolveCall(markers, context, false);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
		this.arguments.resolveTypes(markers, context);

		if (this.genericData != null)
		{
			this.genericData.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveReceiver(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
	}

	@Override
	public void resolveArguments(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);

		if (this.genericData != null)
		{
			this.genericData.resolve(markers, context);
		}
	}

	// every override of this method should call checkArguments on the result; among others to ensure Optional Chain Awareness.
	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		final MatchList<IMethod> candidates = this.resolveCandidates(context);
		if (candidates.hasCandidate())
		{
			return this.checkArguments(markers, context, candidates.getBestMember());
		}

		if (report)
		{
			this.reportResolve(markers, candidates);
			return this;
		}
		return null;
	}

	protected MatchList<IMethod> resolveCandidates(IContext context)
	{
		return ICall.resolveMethods(context, this.receiver, this.getName(), this.arguments);
	}

	protected void reportResolve(MarkerList markers, MatchList<IMethod> matches)
	{
		final Marker marker;
		if (matches == null || !matches.isAmbigous()) // isAmbiguous returns false for empty lists
		{
			marker = Markers.semanticError(this.position, "method.access.resolve", this.getName());
			this.addArgumentInfo(marker);
		}
		else
		{
			marker = Markers.semanticError(this.position, "method.access.ambiguous", this.getName());
			this.addArgumentInfo(marker);

			addCandidates(matches, marker);
		}

		markers.add(marker);
	}

	private void addArgumentInfo(Marker marker)
	{
		if (this.receiver != null)
		{
			marker.addInfo(Markers.getSemantic("receiver.type", this.receiver.getType()));
		}
		if (!this.arguments.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("method.access.argument_types", this.arguments.typesToString()));
		}
	}

	private static void addCandidates(MatchList<IMethod> matches, Marker marker)
	{
		// Duplicate in ConstructorCall.addCandidates

		marker.addInfo(Markers.getSemantic("method.access.candidates"));

		final Candidate<IMethod> first = matches.getCandidate(0);
		addCandidateInfo(marker, first);

		for (int i = 1, count = matches.size(); i < count; i++)
		{
			final Candidate<IMethod> candidate = matches.getCandidate(i);
			if (!candidate.equals(first))
			{
				break;
			}

			addCandidateInfo(marker, candidate);
		}
	}

	private static void addCandidateInfo(Marker marker, Candidate<IMethod> candidate)
	{
		final StringBuilder sb = new StringBuilder().append('\t');
		Util.methodSignatureToString(candidate.getMember(), null, sb);
		marker.addInfo(sb.toString());
	}

	// every resolveCall implementation calls this
	protected final IValue checkArguments(MarkerList markers, IContext context, IMethod method)
	{
		this.method = method;

		final IntrinsicData intrinsicData = method.getIntrinsicData();
		final int code;
		final IValue intrinsic;
		if (intrinsicData != null // Intrinsic annotation
		    && (code = intrinsicData.getCompilerCode()) != 0 // compilerCode argument
		    && (intrinsic = Intrinsics.getOperator(code, this.receiver, this.arguments)) != null) // valid intrinsic
		{
			Deprecation.checkAnnotations(method, this.position, markers);
			intrinsic.setPosition(this.position);
			return intrinsic;
		}

		final GenericData genericData;
		if (this.genericData != null)
		{
			genericData = this.genericData = method.getGenericData(this.genericData, this.receiver, this.arguments);
		}
		else
		{
			genericData = this.getGenericData();
		}

		this.receiver = method.checkArguments(markers, this.position, context, this.receiver, this.arguments,
		                                      genericData);

		this.type = null;
		this.type = this.getType();

		return OptionalChainAware.transform(this);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		this.arguments.checkTypes(markers, context);

		if (this.genericData != null)
		{
			this.genericData.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}

		if (this.method != null)
		{
			this.method
				.checkCall(markers, this.position, context, this.receiver, this.arguments, this.getGenericData());
		}

		this.arguments.check(markers, context);

		if (this.genericData != null)
		{
			this.genericData.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}

		this.arguments.foldConstants();

		if (this.genericData != null)
		{
			this.genericData.foldConstants();
		}

		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(compilableList, classCompilableList);
		}

		this.arguments.cleanup(compilableList, classCompilableList);

		if (this.genericData != null)
		{
			this.genericData.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	// Inlined for performance
	@Override
	public int lineNumber()
	{
		if (this.position == null)
		{
			return 0;
		}
		return this.position.startLine();
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.getType();
		}

		this.method.writeCall(writer, this.receiver, this.arguments, this.genericData, type, this.lineNumber());
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeJump(writer, dest, this.receiver, this.arguments, this.genericData, this.lineNumber());
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeInvJump(writer, dest, this.receiver, this.arguments, this.genericData, this.lineNumber());
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}
