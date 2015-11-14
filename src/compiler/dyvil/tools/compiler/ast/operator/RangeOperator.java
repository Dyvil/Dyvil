package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.statement.loop.IterableForStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class RangeOperator implements IValue
{
	public static final class LazyFields
	{
		public static final IClass			RANGE_CLASS		= Package.dyvilCollection.resolveClass("Range");
		public static final ClassType		RANGE			= new ClassType(RANGE_CLASS);
		public static final IClass			RANGEABLE_CLASS	= Package.dyvilLang.resolveClass("Ordered");
		public static final ClassType		RANGEABLE		= new ClassType(RANGEABLE_CLASS);
		public static final ITypeVariable	RANGEABLE_TYPE	= RANGEABLE_CLASS.getTypeVariable(0);
	}
	
	protected ICodePosition	position;
	protected IValue		firstValue;
	protected IValue		lastValue;
	
	// Metadata
	private boolean	halfOpen;
	private IType	elementType	= Types.UNKNOWN;
	private IType	type;
	
	public RangeOperator(IValue value1, IValue value2)
	{
		this.firstValue = value1;
		this.lastValue = value2;
	}
	
	public RangeOperator(IValue value1, IValue value2, IType type)
	{
		this.firstValue = value1;
		this.lastValue = value2;
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
	
	public void setFirstValue(IValue firstValue)
	{
		this.firstValue = firstValue;
	}
	
	public IValue getFirstValue()
	{
		return this.firstValue;
	}
	
	public void setLastValue(IValue lastValue)
	{
		this.lastValue = lastValue;
	}
	
	public IValue getLastValue()
	{
		return this.lastValue;
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
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.elementType == Types.UNKNOWN)
			{
				this.elementType = Types.combine(this.firstValue.getType(), this.lastValue.getType());
			}
			
			ClassGenericType gt = new ClassGenericType(LazyFields.RANGE_CLASS);
			gt.addType(this.elementType);
			this.type = gt;
		}
		return this.type;
	}
	
	private boolean isElementType(IType elementType)
	{
		if (this.elementType != Types.UNKNOWN)
		{
			return elementType.isSuperTypeOf(this.elementType);
		}
		
		return this.firstValue.isType(elementType) && this.lastValue.isType(elementType);
	}
	
	private static IType getElementType(IType type)
	{
		if (type.isArrayType())
		{
			return type.getElementType();
		}
		if (Types.ITERABLE.isSuperClassOf(type))
		{
			return type.resolveTypeSafely(IterableForStatement.ITERABLE_TYPE);
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
		
		this.type = type;
		this.elementType = elementType;
		
		IValue value1 = this.firstValue.withType(elementType, elementType, markers, context);
		if (value1 == null)
		{
			Util.createTypeError(markers, this.firstValue, elementType, typeContext, "range.start.type");
		}
		else
		{
			this.firstValue = value1;
		}
		
		IValue value2 = this.lastValue.withType(elementType, elementType, markers, context);
		if (value2 == null)
		{
			Util.createTypeError(markers, this.lastValue, elementType, typeContext, "range.end.type");
		}
		else
		{
			this.lastValue = value2;
		}
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		IType elementType = type.getElementType();
		if (elementType == null)
		{
			return false;
		}
		return this.isElementType(elementType);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		IType elementType = getElementType(type);
		if (elementType == null)
		{
			return 0;
		}
		
		float f1 = this.firstValue.getTypeMatch(elementType);
		float f2 = this.lastValue.getTypeMatch(elementType);
		return f1 == 0 || f2 == 0 ? 0 : (f1 + f2) / 2F;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.firstValue.resolveTypes(markers, context);
		this.lastValue.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.firstValue.resolve(markers, context);
		this.lastValue.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.firstValue.checkTypes(markers, context);
		this.lastValue.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.firstValue.check(markers, context);
		this.lastValue.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.firstValue = this.firstValue.foldConstants();
		this.lastValue = this.lastValue.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.firstValue = this.firstValue.cleanup(context, compilableList);
		this.lastValue = this.lastValue.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		// -- Range --
		if (!this.type.isArrayType())
		{
			this.firstValue.writeExpression(writer, LazyFields.RANGEABLE);
			this.lastValue.writeExpression(writer, LazyFields.RANGEABLE);
			
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/collection/Range", this.halfOpen ? "halfOpen" : "apply",
					"(Ldyvil/lang/Rangeable;Ldyvil/lang/Rangeable;)Ldyvil/collection/Range;", false);
			return;
		}
		
		// -- Array --
		String method = this.halfOpen ? "rangeOpen" : "range";
		
		this.firstValue.writeExpression(writer, this.elementType);
		this.lastValue.writeExpression(writer, this.elementType);
		
		// Reference array
		if (!this.elementType.isPrimitive())
		{
			// Write the class instance to be able to reify the type in the
			// method
			String extended = this.elementType.getExtendedName();
			writer.writeLDC(Type.getType(extended));
			
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", method,
					"(Ldyvil/lang/Rangeable;Ldyvil/lang/Rangeable;Ljava/lang/Class;)[Ldyvil/lang/Rangeable;", false);
			
			// CheckCast so the verifier doesn't complain about mismatching types
			writer.writeTypeInsn(Opcodes.CHECKCAST, '[' + extended);
		}
		
		// Primitive array
		switch (this.elementType.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", method, "(ZZ)[Z", false);
			return;
		case PrimitiveType.BYTE_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", method, "(BB)[B", false);
			return;
		case PrimitiveType.SHORT_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", method, "(SS)[S", false);
			return;
		case PrimitiveType.CHAR_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", method, "(CC)[C", false);
			return;
		case PrimitiveType.INT_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", method, "(II)[I", false);
			return;
		case PrimitiveType.LONG_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", method, "(LL)[L", false);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", method, "(FF)[F", false);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", method, "(DD)[D", false);
			return;
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.firstValue.toString(prefix, buffer);
		buffer.append(this.halfOpen ? " ..< " : " .. ");
		this.lastValue.toString(prefix, buffer);
	}
}
