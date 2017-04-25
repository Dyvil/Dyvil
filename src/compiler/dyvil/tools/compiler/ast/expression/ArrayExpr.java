package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.access.ClassAccess;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class ArrayExpr implements IValue
{
	public static final class LazyFields
	{
		public static final IClass ARRAY_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                               .resolveClass(Name.fromRaw("FromArray"));

		private static final TypeChecker.MarkerSupplier ELEMENT_MARKER_SUPPLIER = TypeChecker.markerSupplier(
			"array.element.type.incompatible", "array.element.type.expected", "array.element.type.actual");

		private LazyFields()
		{
			// no instances
		}
	}

	protected @Nullable SourcePosition position;

	protected @NonNull ArgumentList values;

	// Metadata
	protected IType arrayType;
	protected IType elementType;

	public ArrayExpr()
	{
		this.values = ArgumentList.empty();
	}

	public ArrayExpr(ArgumentList values)
	{
		this.values = values;
	}

	public ArrayExpr(SourcePosition position)
	{
		this.position = position;
		this.values = ArgumentList.empty();
	}

	public ArrayExpr(SourcePosition position, int capacity)
	{
		this.position = position;
		this.values = new ArgumentList(capacity);
	}

	public ArrayExpr(SourcePosition position, ArgumentList values)
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
		return ARRAY;
	}

	public ArgumentList getValues()
	{
		return this.values;
	}

	@Override
	public boolean isAnnotationConstant()
	{
		for (IValue value : this.values)
		{
			if (!value.isAnnotationConstant())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isClassAccess()
	{
		return this.values.size() == 1 && this.values.getFirst().isClassAccess();
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		if (!this.isClassAccess())
		{
			return IValue.super.asIgnoredClassAccess();
		}
		return new ClassAccess(this.position, new ArrayType(this.values.getFirst().getType()))
			       .asIgnoredClassAccess();
	}

	public IType getElementType()
	{
		if (this.elementType != null)
		{
			return this.elementType;
		}

		return this.elementType = this.values.getCommonType();
	}

	public void setElementType(IType elementType)
	{
		this.elementType = elementType;
		this.arrayType = new ArrayType(elementType);
	}

	@Override
	public boolean isResolved()
	{
		if (this.arrayType != null)
		{
			return this.arrayType.isResolved();
		}
		if (this.elementType != null)
		{
			return this.elementType.isResolved();
		}

		return this.values.isResolved();
	}

	@Override
	public IType getType()
	{
		if (this.arrayType != null)
		{
			return this.arrayType;
		}

		return this.arrayType = new ArrayType(this.getElementType(), Mutability.IMMUTABLE);
	}

	@Override
	public void setType(IType type)
	{
		this.arrayType = type;
		this.elementType = type.extract(ArrayType.class).getElementType();
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		final int size = this.values.size();
		for (int i = 0; i < size; i++)
		{
			final IValue value = this.values.get(i);
			final IValue constant = value.toAnnotationConstant(markers, context, depth);
			if (constant == null)
			{
				return null;
			}
			else
			{
				this.values.set(i, constant);
			}
		}

		return this;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType elementType;
		final Mutability mutability;

		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType == null)
		{
			final IAnnotation annotation;
			if ((annotation = type.getAnnotation(LazyFields.ARRAY_CONVERTIBLE)) != null)
			{
				return new LiteralConversion(this, annotation, this.values)
					       .withType(type, typeContext, markers, context);
			}
			if (type.getTheClass() != Types.OBJECT_CLASS)
			{
				return null;
			}

			// Compute element type from scratch
			elementType = this.getElementType();
			mutability = Mutability.UNDEFINED;
		}
		else
		{
			// If the type is an array type, get it's element type

			elementType = arrayType.getElementType();
			mutability = type.getMutability();
		}

		final int size = this.values.size();
		if (size == 0)
		{
			this.elementType = elementType.getConcreteType(ITypeContext.DEFAULT);
			this.arrayType = new ArrayType(this.elementType, mutability);
			return this;
		}

		for (int i = 0; i < size; i++)
		{
			final IValue value = TypeChecker.convertValue(this.values.get(i), elementType, typeContext, markers, context,
			                                               LazyFields.ELEMENT_MARKER_SUPPLIER);
			this.values.set(i, value);
		}

		this.elementType = null;
		final int typecode = elementType.getTypecode();
		if (typecode < 0)
		{
			// local element type is an object type, so we infer the reference version of the computed element type
			this.elementType = this.getElementType().getObjectType();
		}
		else if (typecode != this.getElementType().getTypecode())
		{
			// local element type is a primitive type, but a different one from the computed element type,
			// so we infer the local one
			this.elementType = elementType;
		}
		// else: the above call to this.getElementType() has set this.elementType already

		this.arrayType = new ArrayType(this.elementType, mutability);

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType == null)
		{
			return type.getTheClass() == Types.OBJECT_CLASS || type.getAnnotation(LazyFields.ARRAY_CONVERTIBLE) != null;
		}

		final IType elementType = arrayType.getElementType();
		return this.values.isType(elementType);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType == null)
		{
			if (type.getTheClass() == Types.OBJECT_CLASS)
			{
				return SUBTYPE_MATCH;
			}
			if (type.getAnnotation(LazyFields.ARRAY_CONVERTIBLE) != null)
			{
				return CONVERSION_MATCH;
			}
			return MISMATCH;
		}

		final IType elementType = arrayType.getElementType();
		return this.values.getTypeMatch(elementType, implicitContext);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.values.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
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

		if (Types.isVoid(this.getElementType()))
		{
			markers.add(Markers.semanticError(this.position, "array.void"));
		}
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
		final int size = this.values.size();
		final IType elementType = this.getElementType();
		final int arrayStoreOpcode = elementType.getArrayStoreOpcode();

		writer.visitLdcInsn(size);
		writer.visitMultiANewArrayInsn(this.getType(), 1);

		for (int i = 0; i < size; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.values.get(i).writeExpression(writer, elementType);
			writer.visitInsn(arrayStoreOpcode);
		}
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		final AnnotationVisitor arrayVisitor = visitor.visitArray(key);
		for (IValue value : this.values)
		{
			value.writeAnnotationValue(arrayVisitor, null);
		}
		arrayVisitor.visitEnd();
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		final int size = this.values.size();
		if (size == 0)
		{
			if (Formatting.getBoolean("array.empty.space_between"))
			{
				buffer.append("[ ]");
			}
			else
			{
				buffer.append("[]");
			}
			return;
		}

		buffer.append('[');
		if (Formatting.getBoolean("array.open_bracket.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(indent, this.values.getArray(), size, Formatting.getSeparator("array.separator", ','), buffer);

		if (Formatting.getBoolean("array.close_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
