package dyvil.tools.compiler.ast.access;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class AbstractCall implements ICall, IReceiverAccess
{
	protected ICodePosition position;

	protected IValue receiver;
	protected IArguments arguments = EmptyArguments.INSTANCE;
	protected GenericData genericData;

	// Metadata
	protected IMethod method;
	protected IType   type;

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public void setReceiver(IValue receiver)
	{
		this.receiver = receiver;
	}

	@Override
	public IValue getReceiver()
	{
		return this.receiver;
	}

	public abstract Name getName();

	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}

	public void setGenericData(GenericData data)
	{
		this.genericData = data;
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
	public boolean isPrimitive()
	{
		return this.method != null && this.getType().isPrimitive();
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
			this.type = this.method.getType().getConcreteType(this.getGenericData()).asReturnType();
		}
		return this.type;
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
		if (this.arguments.isEmpty())
		{
			this.arguments = EmptyArguments.VISIBLE;
		}
		else
		{
			this.arguments.resolveTypes(markers, context);
		}

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

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		final MatchList<IMethod> ambigousCandidates = this.resolveMethodCall(markers, context);
		if (ambigousCandidates == null)
		{
			return this;
		}

		if (report)
		{
			this.reportResolve(markers, ambigousCandidates);
			return this;
		}
		return null;
	}

	protected MatchList<IMethod> resolveMethodCall(MarkerList markers, IContext context)
	{
		final MatchList<IMethod> list = ICall.resolveMethods(context, this.receiver, this.getName(), this.arguments);
		final IMethod method = list.getBestMember();

		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return null;
		}
		return list;
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

	@Override
	public void checkArguments(MarkerList markers, IContext context)
	{
		if (this.method != null)
		{
			GenericData data;
			if (this.genericData != null)
			{
				data = this.genericData = this.method.getGenericData(this.genericData, this.receiver, this.arguments);
			}
			else
			{
				data = this.getGenericData();
			}

			this.receiver = this.method
				                .checkArguments(markers, this.position, context, this.receiver, this.arguments, data);
		}

		this.type = null;
		this.type = this.getType();
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
	public int getLineNumber()
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

		this.method.writeCall(writer, this.receiver, this.arguments, this.genericData, type, this.getLineNumber());
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeJump(writer, dest, this.receiver, this.arguments, this.genericData, this.getLineNumber());
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeInvJump(writer, dest, this.receiver, this.arguments, this.genericData, this.getLineNumber());
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}
