package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.constant.VoidValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class TupleExpr implements IValue
{
	public static final class LazyFields
	{
		public static final IClass TUPLE_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                               .resolveClass(Name.fromRaw("FromTuple"));

		public static final IClass ENTRY = Package.dyvilCollection.resolveClass("Entry");
		public static final IClass CELL  = Package.dyvilCollection.resolveClass("Cell");

		private static final TypeChecker.MarkerSupplier ELEMENT_MARKER_SUPPLIER = TypeChecker.markerSupplier(
			"tuple.element.type.incompatible", "tuple.element.type.expected", "tuple.element.type.actual");

		private LazyFields()
		{
			// no instances
		}
	}

	protected @Nullable SourcePosition position;

	protected @NonNull ArgumentList values;

	// Metadata
	private @Nullable IType tupleType;

	public TupleExpr(SourcePosition position)
	{
		this.position = position;
		this.values = ArgumentList.empty();
	}

	public TupleExpr(SourcePosition position, ArgumentList values)
	{
		this.position = position;
		this.values = values;
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
		return TUPLE;
	}

	public ArgumentList getValues()
	{
		return this.values;
	}

	@Override
	public boolean isResolved()
	{
		return this.values.isResolved();
	}

	@Override
	public boolean isPartialWildcard()
	{
		return this.values.size() == 1 && this.values.getFirst().isPartialWildcard();
	}

	@Override
	public IValue withLambdaParameter(IParameter parameter)
	{
		return this.values.size() != 1 ? null : this.values.getFirst().withLambdaParameter(parameter);
	}

	@Override
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return this.values.size() != 1 ? null : this.values.getFirst().toAssignment(rhs, position);
	}

	@Override
	public IType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}

		final int arity = this.values.size();
		final TupleType tupleType = new TupleType(arity);
		final TypeList arguments = tupleType.getArguments();
		for (int i = 0; i < arity; i++)
		{
			arguments.add(this.values.get(i).getType());
		}
		return this.tupleType = tupleType;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IAnnotation annotation = type.getAnnotation(LazyFields.TUPLE_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation, this.values).withType(type, typeContext, markers, context);
		}

		final int arity = this.values.size();
		final IClass tupleClass = TupleType.getTupleClass(arity);
		if (!Types.isSuperClass(type, tupleClass.getClassType()))
		{
			return null;
		}

		this.tupleType = null; // reset type

		final TypeParameterList typeParameters = typeParameters(tupleClass, arity);

		for (int i = 0; i < arity; i++)
		{
			final IType elementType = Types.resolveTypeSafely(type, typeParameters.get(i));
			final IValue value = TypeChecker
				                     .convertValue(this.values.get(i), elementType, typeContext, markers, context,
				                                   LazyFields.ELEMENT_MARKER_SUPPLIER);
			this.values.set(i, value);
		}

		return this;
	}

	protected static TypeParameterList typeParameters(IClass tupleClass, int valueCount)
	{
		switch (valueCount)
		{
		case 2:
			return LazyFields.ENTRY.getTypeParameters();
		case 3:
			return LazyFields.CELL.getTypeParameters();
		}
		return tupleClass.getTypeParameters();
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType()) || type.getAnnotation(LazyFields.TUPLE_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int arity = this.values.size();
		if (arity == 1)
		{
			return this.values.getFirst().getTypeMatch(type, implicitContext);
		}

		final IClass tupleClass = TupleType.getTupleClass(arity);
		if (!Types.isSuperClass(type, tupleClass.getClassType()))
		{
			if (type.getAnnotation(LazyFields.TUPLE_CONVERTIBLE) != null)
			{
				return CONVERSION_MATCH;
			}
			return MISMATCH;
		}

		this.tupleType = null; // reset type

		final TypeParameterList typeParameters = typeParameters(tupleClass, arity);

		int min = EXACT_MATCH;
		for (int i = 0; i < arity; i++)
		{
			final IType elementType = Types.resolveTypeSafely(type, typeParameters.get(i));
			final int match = TypeChecker.getTypeMatch(this.values.get(i), elementType, implicitContext);

			if (match == MISMATCH)
			{
				return MISMATCH;
			}
			if (match < min)
			{
				min = match;
			}
		}
		return min;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.values.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		switch (this.values.size())
		{
		case 0:
			return new VoidValue(this.position);
		case 1:
			return this.values.getFirst().resolve(markers, context);
		}

		this.values.resolve(markers, context);

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.values.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.values.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.values.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.values.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final IType thisType = this.getType();
		final String internal = thisType.getInternalName();
		writer.visitTypeInsn(Opcodes.NEW, internal);
		writer.visitInsn(Opcodes.DUP);

		final int arity = this.values.size();
		for (int i = 0; i < arity; i++)
		{
			this.values.get(i).writeExpression(writer, Types.OBJECT);
		}

		final String desc = TupleType.getConstructorDescriptor(arity);
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, internal, "<init>", desc, false);

		if (type != null)
		{
			thisType.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.values.toString(indent, buffer);
	}
}
