package dyvil.tools.compiler.ast.expression;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
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
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.util.Iterator;

public final class ArrayExpr implements IValue, IValueList
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

	protected ICodePosition position;

	protected IValue[] values;
	protected int      valueCount;

	// Metadata
	protected IType arrayType;
	protected IType elementType;

	public ArrayExpr()
	{
		this.values = new IValue[4];
	}

	public ArrayExpr(IValue[] values, int valueCount)
	{
		this.values = values;
		this.valueCount = valueCount;
	}

	public ArrayExpr(ICodePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
	}

	public ArrayExpr(ICodePosition position, int capacity)
	{
		this.position = position;
		this.values = new IValue[capacity];
	}

	public ArrayExpr(ICodePosition position, IValue[] values, int valueCount)
	{
		this.position = position;
		this.values = values;
		this.valueCount = valueCount;
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
		return ARRAY;
	}

	@Override
	public boolean isAnnotationConstant()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isAnnotationConstant())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}

	@Override
	public boolean isClassAccess()
	{
		return this.valueCount == 1 && this.values[0].isClassAccess();
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		if (!this.isClassAccess())
		{
			return IValue.super.asIgnoredClassAccess();
		}
		return new ClassAccess(this.position, new ArrayType(this.values[0].getType())).asIgnoredClassAccess();
	}

	public IType getElementType()
	{
		if (this.elementType != null)
		{
			return this.elementType;
		}

		return this.elementType = getCommonType(this.values, this.valueCount);
	}

	public static IType getCommonType(IValue[] values, int valueCount)
	{
		if (valueCount == 0)
		{
			return Types.ANY;
		}

		IType type = values[0].getType();
		for (int i = 1; i < valueCount; i++)
		{
			final IType valueType = values[i].getType();
			type = Types.combine(type, valueType);

			if (type == null)
			{
				return Types.ANY;
			}
		}

		return type;
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

		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isResolved())
			{
				return false;
			}
		}
		return true;
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
		this.elementType = type.getElementType();
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			final IValue value = this.values[i];
			final IValue constant = value.toAnnotationConstant(markers, context, depth);
			if (constant == null)
			{
				return null;
			}
			else
			{
				this.values[i] = constant;
			}
		}

		return this;
	}

	@Override
	public IValue withType(IType arrayType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType elementType;
		final Mutability mutability;
		if (!arrayType.isArrayType())
		{
			final IAnnotation annotation;
			if ((annotation = arrayType.getAnnotation(LazyFields.ARRAY_CONVERTIBLE)) != null)
			{
				return new LiteralConversion(this, annotation, new ArgumentList(this.values, this.valueCount))
					       .withType(arrayType, typeContext, markers, context);
			}
			if (arrayType.getTheClass() != Types.OBJECT_CLASS)
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
			mutability = arrayType.getMutability();
		}

		if (this.valueCount == 0)
		{
			this.elementType = elementType.getConcreteType(ITypeContext.DEFAULT);
			this.arrayType = new ArrayType(this.elementType, mutability);
			return this;
		}

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = TypeChecker.convertValue(this.values[i], elementType, typeContext, markers, context,
			                                          LazyFields.ELEMENT_MARKER_SUPPLIER);
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
		if (!type.isArrayType())
		{
			return type.getTheClass() == Types.OBJECT_CLASS || type.getAnnotation(LazyFields.ARRAY_CONVERTIBLE) != null;
		}

		// Skip getting the element type if this is an empty array
		if (this.valueCount == 0)
		{
			return true;
		}

		// If the type is an array type, get it's element type
		final IType elementType = type.getElementType();

		// Check for every value if it is the element type
		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isType(elementType))
			{
				// If not, this is not the type
				return false;
			}
		}

		return true;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		if (!type.isArrayType())
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

		// Skip getting the element type if this is an empty array
		if (this.valueCount == 0)
		{
			return EXACT_MATCH;
		}

		// If the type is an array type, get it's element type
		final IType elementType = type.getElementType();

		int min = Integer.MAX_VALUE;
		for (int i = 0; i < this.valueCount; i++)
		{
			// TODO Implicit conversions?
			final int match = this.values[i].getTypeMatch(elementType);
			if (match == MISMATCH)
			{
				// If any element type has a mismatch, produce a mismatch
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
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, this.valueCount);
	}

	@Override
	public int valueCount()
	{
		return this.valueCount;
	}

	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}

	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}

	@Override
	public void addValue(IValue value)
	{
		int index = this.valueCount++;
		if (index >= this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}

	@Override
	public void addValue(int index, IValue value)
	{
		IValue[] temp = new IValue[++this.valueCount];
		System.arraycopy(this.values, 0, temp, 0, index);
		temp[index] = value;
		System.arraycopy(this.values, index, temp, index + 1, this.valueCount - index - 1);
		this.values = temp;
	}

	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}

		if (Types.isVoid(this.getElementType()))
		{
			markers.add(Markers.semanticError(this.position, "array.void"));
		}
	}

	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final IType elementType = this.getElementType();
		final int arrayStoreOpcode = elementType.getArrayStoreOpcode();

		writer.visitLdcInsn(this.valueCount);
		writer.visitMultiANewArrayInsn(this.getType(), 1);

		for (int i = 0; i < this.valueCount; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.values[i].writeExpression(writer, elementType);
			writer.visitInsn(arrayStoreOpcode);
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount == 0)
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

		Util.astToString(prefix, this.values, this.valueCount, Formatting.getSeparator("array.separator", ','), buffer);

		if (Formatting.getBoolean("array.close_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
