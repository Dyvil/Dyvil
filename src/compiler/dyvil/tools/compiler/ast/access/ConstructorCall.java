package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ConstructorCall implements ICall
{
	protected ICodePosition position;
	protected IType         type;
	protected IArguments    arguments;

	protected IConstructor constructor;

	public ConstructorCall()
	{
		this.arguments = EmptyArguments.INSTANCE;
	}

	public ConstructorCall(IConstructor constructor, IArguments arguments)
	{
		this.constructor = constructor;
		this.type = constructor.getType();
		this.arguments = arguments;
	}

	public ConstructorCall(ICodePosition position)
	{
		this.position = position;
		this.arguments = EmptyArguments.INSTANCE;
	}

	public ConstructorCall(ICodePosition position, IType type, IArguments arguments)
	{
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}

	public ConstructorCall(ICodePosition position, IConstructor constructor, IArguments arguments)
	{
		this.position = position;
		this.constructor = constructor;
		this.type = constructor.getType();
		this.arguments = arguments;
	}

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
	public int valueTag()
	{
		return CONSTRUCTOR_CALL;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
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
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}

	public ClassConstructor toClassConstructor()
	{
		ClassConstructor cc = new ClassConstructor(this.position);
		cc.type = this.type;
		cc.constructor = this.constructor;
		cc.arguments = this.arguments;
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
			markers.add(Markers.semantic(this.position, "constructor.invalid"));
			this.type = Types.UNKNOWN;
		}

		if (this.arguments.isEmpty())
		{
			this.arguments = EmptyArguments.VISIBLE;
		}
		else
		{
			this.arguments.resolveTypes(markers, context);
		}
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

		final MatchList<IConstructor> ambigousConstructors = this.resolveConstructor(markers, context, this.type);
		if (ambigousConstructors == null)
		{
			return this;
		}

		if (report)
		{
			reportResolve(markers, ambigousConstructors, this.position, this.type, this.arguments);
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
			final IValue value = this.arguments.getValue(i, null);
			final IValue typed = TypeChecker.convertValue(value, Types.INT, ITypeContext.DEFAULT, markers, context,
			                                              TypeChecker.markerSupplier("constructor.access.array.type"));
			this.arguments.setValue(i, null, typed);
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

	protected MatchList<IConstructor> resolveConstructor(MarkerList markers, IContext context, IType type)
	{
		final MatchList<IConstructor> list = IContext.resolveConstructors(context, type, this.arguments);
		final IConstructor constructor = list.getBestMember();

		if (constructor != null)
		{
			this.constructor = constructor;
			this.checkArguments(markers, context);
			return null;
		}
		return list;
	}

	protected static void reportResolve(MarkerList markers, MatchList<IConstructor> list, ICodePosition position, IType type, IArguments arguments)
	{
		final Marker marker;
		if (list == null || !list.isAmbigous())
		{
			marker = Markers.semanticError(position, "constructor.access.resolve", type.toString());
			addArgumentInfo(marker, arguments);
		}
		else
		{
			marker = Markers.semanticError(position, "constructor.access.ambiguous", type.toString());
			addArgumentInfo(marker, arguments);
			addCandidates(list, marker);
		}

		markers.add(marker);
	}

	private static void addArgumentInfo(Marker marker, IArguments arguments)
	{
		if (!arguments.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("method.access.argument_types", arguments.typesToString()));
		}
	}

	protected static void addCandidates(MatchList<IConstructor> matches, Marker marker)
	{
		// Duplicate in AbstractCall.addCandidates

		marker.addInfo(Markers.getSemantic("method.access.candidates"));

		final Candidate<IConstructor> first = matches.getCandidate(0);
		addCandidateInfo(marker, first);

		for (int i = 1, count = matches.size(); i < count; i++)
		{
			final Candidate<IConstructor> candidate = matches.getCandidate(i);
			if (!candidate.equals(first))
			{
				break;
			}

			addCandidateInfo(marker, candidate);
		}
	}

	protected static void addCandidateInfo(Marker marker, Candidate<IConstructor> candidate)
	{
		final StringBuilder sb = new StringBuilder().append('\t');
		Util.constructorSignatureToString(candidate.getMember(), null, sb);
		marker.addInfo(sb.toString());
	}

	@Override
	public void checkArguments(MarkerList markers, IContext context)
	{
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
			if (iclass.hasModifier(Modifiers.ABSTRACT) && iclass.hasModifier(Modifiers.INTERFACE_CLASS))
			{
				markers.add(Markers.semantic(this.position, "constructor.access.abstract", this.type));
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
			this.constructor.writeCall(writer, this.arguments, type, this.getLineNumber());
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
			this.arguments.getValue(i, null).writeExpression(writer, Types.INT);
		}

		writer.visitMultiANewArrayInsn(arrayType, count);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		this.arguments.toString(prefix, buffer);
	}
}
