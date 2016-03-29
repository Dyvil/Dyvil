package dyvil.tools.compiler.ast.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.statement.loop.IterableForStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class RangeOperator implements IValue
{
	public static final class LazyFields
	{
		public static final IClass         RANGE_CLASS     = Package.dyvilCollection.resolveClass("Range");
		public static final IClass         RANGEABLE_CLASS = Package.dyvilCollectionRange.resolveClass("Rangeable");
		public static final IType          RANGEABLE       = RANGEABLE_CLASS.getClassType();

		public static final IClass INT_RANGE_CLASS    = Package.dyvilCollectionRange.resolveClass("IntRange");
		public static final IClass LONG_RANGE_CLASS   = Package.dyvilCollectionRange.resolveClass("LongRange");
		public static final IClass FLOAT_RANGE_CLASS  = Package.dyvilCollectionRange.resolveClass("FloatRange");
		public static final IClass DOUBLE_RANGE_CLASS = Package.dyvilCollectionRange.resolveClass("DoubleRange");
	}

	protected ICodePosition position;
	protected IValue        startValue;
	protected IValue        endValue;

	// Metadata
	private boolean halfOpen;
	private IType elementType = Types.UNKNOWN;
	private IType type;

	public RangeOperator(IValue value1, IValue value2)
	{
		this.startValue = value1;
		this.endValue = value2;
	}

	public RangeOperator(IValue value1, IValue value2, IType type)
	{
		this.startValue = value1;
		this.endValue = value2;
		this.elementType = type;
	}

	public void setHalfOpen(boolean halfOpen)
	{
		this.halfOpen = halfOpen;
	}

	public boolean isHalfOpen()
	{
		return this.halfOpen;
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
		return RANGE_OPERATOR;
	}

	public void setStartValue(IValue startValue)
	{
		this.startValue = startValue;
	}

	public IValue getStartValue()
	{
		return this.startValue;
	}

	public void setEndValue(IValue endValue)
	{
		this.endValue = endValue;
	}

	public IValue getEndValue()
	{
		return this.endValue;
	}

	public IType getElementType()
	{
		return this.elementType;
	}

	@Override
	public boolean isResolved()
	{
		return this.type != null && this.type.isResolved();
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.startValue.hasSideEffects() || this.endValue.hasSideEffects();
	}

	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}

		if (this.elementType == Types.UNKNOWN)
		{
			this.elementType = Types.combine(this.startValue.getType(), this.endValue.getType());
		}

		if (this.elementType.isPrimitive())
		{
			switch (this.elementType.getTypecode())
			{
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				this.elementType = Types.INT;
				return this.type = LazyFields.INT_RANGE_CLASS.getClassType();
			case PrimitiveType.LONG_CODE:
				this.elementType = Types.LONG;
				return this.type = LazyFields.LONG_RANGE_CLASS.getClassType();
			case PrimitiveType.FLOAT_CODE:
				this.elementType = Types.FLOAT;
				return this.type = LazyFields.FLOAT_RANGE_CLASS.getClassType();
			case PrimitiveType.DOUBLE_CODE:
				this.elementType = Types.DOUBLE;
				return this.type = LazyFields.DOUBLE_RANGE_CLASS.getClassType();
			}
		}

		final ClassGenericType genericType = new ClassGenericType(LazyFields.RANGE_CLASS);
		genericType.addType(this.elementType);
		this.type = genericType;
		return this.type;
	}

	private boolean isElementType(IType elementType)
	{
		if (this.elementType != Types.UNKNOWN)
		{
			return Types.isSuperType(elementType, this.elementType);
		}

		return this.startValue.isType(elementType) && this.endValue.isType(elementType);
	}

	private static IType getElementType(IType type)
	{
		if (type.isArrayType())
		{
			return type.getElementType();
		}
		if (Types.isSuperType(IterableForStatement.LazyFields.ITERABLE, type))
		{
			return type.resolveTypeSafely(IterableForStatement.LazyFields.ITERABLE_TYPE);
		}
		return null;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IType elementType = getElementType(type);
		if (elementType == null)
		{
			return null;
		}
		elementType = PrimitiveType.getPrimitiveType(elementType);

		this.type = type;
		this.elementType = elementType;

		this.startValue = TypeChecker.convertValue(this.startValue, elementType, typeContext, markers, context,
		                                           TypeChecker.markerSupplier("range.start.type"));

		this.endValue = TypeChecker.convertValue(this.endValue, elementType, typeContext, markers, context,
		                                         TypeChecker.markerSupplier("range.end.type"));

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		IType elementType = type.getElementType();
		return elementType != null && this.isElementType(elementType);
	}

	@Override
	public int getTypeMatch(IType type)
	{
		IType elementType = getElementType(type);
		if (elementType == null)
		{
			return 0;
		}

		int f1 = this.startValue.getTypeMatch(elementType);
		int f2 = this.endValue.getTypeMatch(elementType);
		return f1 == 0 || f2 == 0 ? 0 : (f1 + f2) / 2;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.startValue.resolveTypes(markers, context);
		this.endValue.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.startValue.resolve(markers, context);
		this.endValue.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.startValue.checkTypes(markers, context);
		this.endValue.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.startValue.check(markers, context);
		this.endValue.check(markers, context);

		final IType elementType = this.getElementType();
		if (!elementType.isResolved())
		{
			return;
		}

		if (elementType.isPrimitive())
		{
			switch (elementType.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
			case PrimitiveType.VOID_CODE:
				break;
			default:
				return;
			}
		}

		if (Types.isSuperType(LazyFields.RANGEABLE, elementType))
		{
			return;
		}

		final Marker marker = Markers.semanticError(this.position, "range.element.type.incompatible");
		marker.addInfo(Markers.getSemantic("range.element.type", elementType));
		markers.add(marker);
	}

	@Override
	public IValue foldConstants()
	{
		this.startValue = this.startValue.foldConstants();
		this.endValue = this.endValue.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.startValue = this.startValue.cleanup(context, compilableList);
		this.endValue = this.endValue.cleanup(context, compilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		// -- Range --
		if (!this.type.isArrayType())
		{
			this.writeRangeExpression(writer);
			return;
		}

		// -- Array --
		this.writeArrayExpression(writer);
	}

	private void writeRangeExpression(MethodWriter writer) throws BytecodeException
	{
		final String method = this.halfOpen ? "halfOpen" : "apply";

		if (Types.isSuperType(LazyFields.RANGEABLE, this.getElementType()))
		{
			this.startValue.writeExpression(writer, LazyFields.RANGEABLE_CLASS.getClassType());
			this.endValue.writeExpression(writer, LazyFields.RANGEABLE_CLASS.getClassType());

			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/Range", method,
			                       "(Ldyvil/collection/range/Rangeable;Ldyvil/collection/range/Rangeable;)Ldyvil/collection/Range;",
			                       false);
			return;
		}

		this.startValue.writeExpression(writer, this.elementType);
		this.endValue.writeExpression(writer, this.elementType);

		switch (this.elementType.getTypecode())
		{
		case PrimitiveType.BYTE_CODE:
		case PrimitiveType.SHORT_CODE:
		case PrimitiveType.CHAR_CODE:
		case PrimitiveType.INT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/IntRange", method,
			                       "(II)Ldyvil/collection/range/IntRange;", false);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/LongRange", method,
			                       "(JJ)Ldyvil/collection/range/LongRange;", false);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/FloatRange", method,
			                       "(FF)Ldyvil/collection/range/FloatRange;", false);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/DoubleRange", method,
			                       "(DD)Ldyvil/collection/range/DoubleRange;", false);
			return;
		}
	}

	private void writeArrayExpression(MethodWriter writer) throws BytecodeException
	{
		final String method = this.halfOpen ? "rangeOpen" : "range";

		this.startValue.writeExpression(writer, this.elementType);
		this.endValue.writeExpression(writer, this.elementType);

		// Reference array
		if (!this.elementType.isPrimitive())
		{
			// Write the class instance to be able to reify the type in the
			// method
			String extended = this.elementType.getExtendedName();
			writer.visitLdcInsn(Type.getType(extended));

			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", method,
			                       "(Ldyvil/collection/range/Rangeable;Ldyvil/collection/range/Rangeable;Ljava/lang/Class;)[Ldyvil/collection/range/Rangeable;",
			                       false);

			// CheckCast so the verifier doesn't complain about mismatching types
			writer.visitTypeInsn(Opcodes.CHECKCAST, '[' + extended);
			return;
		}

		// Primitive array
		switch (this.elementType.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", method, "(ZZ)[Z", false);
			return;
		case PrimitiveType.BYTE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", method, "(BB)[B", false);
			return;
		case PrimitiveType.SHORT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", method, "(SS)[S", false);
			return;
		case PrimitiveType.CHAR_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", method, "(CC)[C", false);
			return;
		case PrimitiveType.INT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", method, "(II)[I", false);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", method, "(LL)[L", false);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", method, "(FF)[F", false);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", method, "(DD)[D", false);
			return;
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.startValue.toString(prefix, buffer);
		buffer.append(this.halfOpen ? " ..< " : " .. ");
		this.endValue.toString(prefix, buffer);
	}
}
