package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class ConstructorCall implements ICall
{
	protected @NonNull IType        type;
	protected @NonNull ArgumentList arguments;

	// Metadata
	protected SourcePosition position;
	protected IConstructor   constructor;

	public ConstructorCall(SourcePosition position)
	{
		this.position = position;
	}

	public ConstructorCall(SourcePosition position, ArgumentList arguments)
	{
		this.position = position;
		this.arguments = arguments;
	}

	public ConstructorCall(IConstructor constructor, ArgumentList arguments)
	{
		this.constructor = constructor;
		this.type = constructor.getType();
		this.arguments = arguments;
	}

	public ConstructorCall(SourcePosition position, IType type, ArgumentList arguments)
	{
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}

	public ConstructorCall(SourcePosition position, IConstructor constructor, ArgumentList arguments)
	{
		this.position = position;
		this.constructor = constructor;
		this.type = constructor.getType();
		this.arguments = arguments;
	}

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
	public int valueTag()
	{
		return CONSTRUCTOR_CALL;
	}

	public IConstructor getConstructor()
	{
		return this.constructor;
	}

	@Override
	public boolean isResolved()
	{
		return this.constructor != null;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public void setArguments(ArgumentList arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public ArgumentList getArguments()
	{
		return this.arguments;
	}

	public ClassConstructorCall toClassConstructor()
	{
		ClassConstructorCall cc = new ClassConstructorCall(this.position, this.type, this.arguments);
		cc.constructor = this.constructor;
		return cc;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "constructor.access.type.missing"));
			this.type = Types.UNKNOWN;
		}

		this.arguments.resolveTypes(markers, context);
	}

	@Override
	public void resolveArguments(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.arguments.resolve(markers, context);
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		if (!this.type.isResolved())
		{
			return this;
		}

		final ArrayType arrayType = this.type.extract(ArrayType.class);
		if (arrayType != null)
		{
			this.resolveArrayConstructor(markers, context, arrayType);
			return this;
		}

		final IClass theClass = this.type.getTheClass();
		if (theClass != null && theClass.isInterface())
		{
			if (!report)
			{
				return null;
			}

			markers.add(Markers.semanticError(this.position, "constructor.access.interface", this.type));
			return this;
		}

		final MatchList<IConstructor> candidates = this.resolveCandidates(context, this.type);
		if (candidates.hasCandidate())
		{
			this.checkArguments(markers, context, candidates.getBestMember());
			return this;
		}

		if (report)
		{
			reportResolve(markers, candidates, this.position, this.type, this.arguments);
			return this;
		}
		return null;
	}

	private void resolveArrayConstructor(MarkerList markers, IContext context, ArrayType arrayType)
	{
		final IType elementType = arrayType.getElementType();
		if (elementType.hasTag(IType.TYPE_VAR))
		{
			markers.add(Markers.semanticError(this.position, "constructor.access.array.typevar", elementType));
		}

		final int len = this.arguments.size();
		final int dims = 1 + getDimensions(arrayType.getElementType());
		if (len > dims)
		{
			final Marker marker = Markers.semanticError(this.position, "constructor.access.array.length");
			marker.addInfo(Markers.getSemantic("type.dimensions", dims));
			marker.addInfo(Markers.getSemantic("constructor.access.array.count", len));
			markers.add(marker);
			return;
		}

		for (int i = 0; i < len; i++)
		{
			final IValue value = this.arguments.get(i, null);
			final IValue typed = TypeChecker.convertValue(value, Types.INT, ITypeContext.DEFAULT, markers, context,
			                                              TypeChecker.markerSupplier("constructor.access.array.type"));
			this.arguments.set(i, null, typed);
		}
	}

	private static int getDimensions(IType type)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType != null)
		{
			return 1 + getDimensions(arrayType.getElementType());
		}
		return 0;
	}

	protected MatchList<IConstructor> resolveCandidates(IContext context, IType type)
	{
		return IContext.resolveConstructors(context, type, this.arguments);
	}

	protected static void reportResolve(MarkerList markers, MatchList<IConstructor> list, SourcePosition position,
		                                   IType type, ArgumentList arguments)
	{
		final Marker marker;
		if (list != null && list.isAmbigous())
		{
			marker = Markers.semanticError(position, "constructor.access.ambiguous", type.toString());
		}
		else // isAmbiguous returns false for empty lists
		{
			marker = Markers.semanticError(position, "constructor.access.resolve", type.toString());
		}

		addArgumentInfo(marker, arguments);
		addCandidates(list, marker);

		markers.add(marker);
	}

	private static void addArgumentInfo(Marker marker, ArgumentList arguments)
	{
		if (!arguments.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("method.access.argument_types", arguments.typesToString()));
		}
	}

	protected static void addCandidates(MatchList<IConstructor> matches, Marker marker)
	{
		if (matches == null || matches.isEmpty())
		{
			return;
		}

		marker.addInfo(Markers.getSemantic("method.access.candidates"));

		for (Candidate<IConstructor> candidate : matches.getAmbiguousCandidates())
		{
			final StringBuilder builder = new StringBuilder().append('\t');
			Util.constructorSignatureToString(candidate.getMember(), null, builder);
			marker.addInfo(builder.toString());
		}
	}

	public void checkArguments(MarkerList markers, IContext context, IConstructor constructor)
	{
		this.constructor = constructor;
		this.type = this.constructor.checkArguments(markers, this.position, context, this.type, this.arguments);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		this.arguments.check(markers, context);

		if (this.type.hasTag(IType.ARRAY))
		{
			return;
		}

		if (this.constructor != null)
		{
			final IClass iclass = this.type.getTheClass();
			if (iclass.isInterface())
			{
				markers.add(Markers.semanticError(this.position, "constructor.access.abstract", this.type));
			}

			this.constructor.checkCall(markers, this.position, context, this.arguments);
		}
	}

	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.arguments.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (!this.type.hasTag(IType.ARRAY))
		{
			this.constructor.writeCall(writer, this.arguments, type, this.lineNumber());
			return;
		}

		final IType arrayType = this.getType();
		final int count = this.arguments.size();

		if (count == 0)
		{
			writer.visitLdcInsn(0);
			writer.visitMultiANewArrayInsn(arrayType, 1);
			return;
		}

		for (int i = 0; i < count; i++)
		{
			this.arguments.get(i, null).writeExpression(writer, Types.INT);
		}

		writer.visitMultiANewArrayInsn(arrayType, count);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		this.arguments.toString(indent, buffer);
	}
}
