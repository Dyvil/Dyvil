package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.statement.foreach.IterableForStatement;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class RangeOperator implements IValue
{
	public static final IClass			RANGE_CLASS		= Package.dyvilLang.resolveClass("Range");
	public static final ClassType			RANGE			= new ClassType(RANGE_CLASS);
	public static final IClass			ORDERED_CLASS	= Package.dyvilLang.resolveClass("Ordered");
	public static final ClassType			ORDERED			= new ClassType(ORDERED_CLASS);
	private static final ITypeVariable	ORDERED_TYPE	= ORDERED_CLASS.getTypeVariable(0);
	
	public IValue						value1;
	public IValue						value2;
	private IType						elementType		= Types.UNKNOWN;
	private IType						type;
	
	public RangeOperator(IValue value1, IValue value2)
	{
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public RangeOperator(IValue value1, IValue value2, IType type)
	{
		this.value1 = value1;
		this.value2 = value2;
		this.elementType = type;
	}
	
	@Override
	public int valueTag()
	{
		return RANGE_OPERATOR;
	}
	
	public IType getElementType()
	{
		return this.elementType;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.elementType == Types.UNKNOWN)
			{
				this.elementType = Types.findCommonSuperType(this.value1.getType(), this.value2.getType());
			}
			
			GenericType gt = new GenericType(RANGE_CLASS);
			
			if (this.elementType.isPrimitive())
			{
				this.elementType = this.elementType.getReferenceType();
				this.value1 = this.value1.withType(this.elementType);
				this.value2 = this.value2.withType(this.elementType);
			}
			gt.addType(this.elementType);
			this.type = gt;
		}
		return this.type;
	}
	
	private boolean isElementType(IType elementType)
	{
		if (this.elementType != null)
		{
			return elementType.isSuperTypeOf(this.elementType);
		}
		
		return this.value1.isType(elementType) && this.value2.isType(elementType);
	}
	
	private IValue withElementType(IType type, IType elementType)
	{
		if (!this.value1.isType(elementType))
		{
			return null;
		}
		if (!this.value2.isType(elementType))
		{
			return null;
		}
		this.type = type;
		this.elementType = elementType;
		return this;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type.isArrayType())
		{
			IType elementType = type.getElementType();
			return this.withElementType(type, elementType);
		}
		if (Types.ITERABLE.equals(type) || RANGE.isSuperTypeOf(type))
		{
			IType iterableType = type.resolveType(IterableForStatement.ITERABLE_TYPE);
			return this.withElementType(type, iterableType);
		}
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.isArrayType())
		{
			IType elementType = type.getElementType();
			return this.isElementType(elementType);
		}
		if (Types.ITERABLE.equals(type) || RANGE.isSuperTypeOf(type))
		{
			IType iterableType = type.resolveType(IterableForStatement.ITERABLE_TYPE);
			return this.isElementType(iterableType);
		}
		return type.isSuperTypeOf(RANGE);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.isArrayType())
		{
			IType elementType = type.getElementType();
			return this.isElementType(elementType) ? 3 : 0;
		}
		if (Types.ITERABLE.equals(type) || RANGE.isSuperTypeOf(type))
		{
			IType iterableType = type.resolveType(IterableForStatement.ITERABLE_TYPE);
			return this.isElementType(iterableType) ? 3 : 0;
		}
		return type.isSuperTypeOf(RANGE) ? 2 : 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value1.resolveTypes(markers, context);
		this.value2.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value1.resolve(markers, context);
		this.value2.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IValue value1 = this.value1.withType(this.elementType);
		if (value1 == null)
		{
			// TODO Handle error?
		}
		else
		{
			this.value1 = value1;
		}
		
		value1 = this.value2.withType(this.elementType);
		if (value1 == null)
		{
			// ...
		}
		else
		{
			this.value2 = value1;
		}
		
		this.value1.checkTypes(markers, context);
		this.value2.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value1.check(markers, context);
		this.value2.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.type.isArrayType())
		{
			this.value1.writeExpression(writer);
			this.value2.writeExpression(writer);
			
			if (this.elementType.typeTag() == IType.PRIMITIVE)
			{
				switch (((PrimitiveType) this.elementType).typecode)
				{
				case ClassFormat.T_BYTE:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "range", "(BB)[B", false);
					return;
				case ClassFormat.T_SHORT:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "range", "(SS)[S", false);
					return;
				case ClassFormat.T_CHAR:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "range", "(CC)[C", false);
					return;
				case ClassFormat.T_INT:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "range", "(II)[I", false);
					return;
				case ClassFormat.T_LONG:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "range", "(LL)[L", false);
					return;
				case ClassFormat.T_FLOAT:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "range", "(FF)[F", false);
					return;
				case ClassFormat.T_DOUBLE:
					writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "range", "(DD)[D", false);
					return;
				}
				
				return;
			}
			if (Types.STRING.isSuperTypeOf(this.elementType))
			{
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "range", "(Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;",
						false);
				return;
			}
			
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "range", "(Ldyvil/lang/Ordered;Ldyvil/lang/Ordered;)[Ldyvil/lang/Ordered;",
					false);
			return;
		}
		
		if (Types.STRING.isSuperTypeOf(this.elementType))
		{
			writer.writeTypeInsn(Opcodes.NEW, "dyvil/collection/range/StringRange");
			writer.writeInsn(Opcodes.DUP);
			this.value1.writeExpression(writer);
			this.value2.writeExpression(writer);
			writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/collection/range/StringRange", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false);
			return;
		}
		
		writer.writeTypeInsn(Opcodes.NEW, "dyvil/collection/range/SimpleRange");
		writer.writeInsn(Opcodes.DUP);
		this.value1.writeExpression(writer);
		this.value2.writeExpression(writer);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/collection/range/SimpleRange", "<init>", "(Ldyvil/lang/Ordered;Ldyvil/lang/Ordered;)V", false);
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
		this.value1.toString(prefix, buffer);
		buffer.append(" .. ");
		this.value2.toString(prefix, buffer);
	}
}
