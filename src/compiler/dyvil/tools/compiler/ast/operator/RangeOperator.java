package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.statement.foreach.IterableForStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class RangeOperator implements IValue
{
	public static final IClass			RANGE_CLASS		= Package.dyvilLang.resolveClass("Range");
	public static final ClassType		RANGE			= new ClassType(RANGE_CLASS);
	public static final IClass			ORDERED_CLASS	= Package.dyvilLang.resolveClass("Ordered");
	public static final ClassType		ORDERED			= new ClassType(ORDERED_CLASS);
	private static final ITypeVariable	ORDERED_TYPE	= ORDERED_CLASS.getTypeVariable(0);
	
	public ICodePosition				position;
	protected IValue					firstValue;
	protected IValue					lastValue;
	private IType						elementType		= Types.UNKNOWN;
	private IType						type;
	
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
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.elementType == Types.UNKNOWN)
			{
				this.elementType = Types.combine(this.firstValue.getType(), this.lastValue.getType());
			}
			
			ClassGenericType gt = new ClassGenericType(RANGE_CLASS);
			if (this.elementType.isPrimitive())
			{
				this.elementType = this.elementType.getReferenceType();
			}
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
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IType elementType;
		if (type.isArrayType())
		{
			elementType = type.getElementType();
		}
		else if (type.isSuperClassOf(RANGE))
		{
			elementType = type.resolveType(IterableForStatement.ITERABLE_TYPE);
		}
		else
		{
			return null;
		}
		
		if (this.isElementType(elementType))
		{
			this.elementType = elementType;
			this.type = type;
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.isArrayType())
		{
			IType elementType = type.getElementType();
			return this.isElementType(elementType);
		}
		if (type.isSuperClassOf(RANGE))
		{
			IType iterableType = type.resolveType(IterableForStatement.ITERABLE_TYPE);
			return this.isElementType(iterableType);
		}
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
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
		IValue value1 = this.firstValue.withType(this.elementType, this.elementType, markers, context);
		if (value1 == null)
		{
			// TODO Handle error
		}
		else
		{
			this.firstValue = value1;
		}
		
		IValue value2 = this.lastValue.withType(this.elementType, this.elementType, markers, context);
		if (value2 == null)
		{
			// TODO Handle error
		}
		else
		{
			this.lastValue = value2;
		}
		
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
		if (this.type.isArrayType())
		{
			this.firstValue.writeExpression(writer);
			this.lastValue.writeExpression(writer);
			
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
			if (this.elementType.getTheClass() == Types.STRING_CLASS)
			{
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "range", "(Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;",
						false);
				return;
			}
			
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "range", "(Ldyvil/lang/Ordered;Ldyvil/lang/Ordered;)[Ldyvil/lang/Ordered;",
					false);
			return;
		}
		
		if (this.elementType.getTheClass() == Types.STRING_CLASS)
		{
			writer.writeTypeInsn(Opcodes.NEW, "dyvil/collection/range/StringRange");
			writer.writeInsn(Opcodes.DUP);
			this.firstValue.writeExpression(writer);
			this.lastValue.writeExpression(writer);
			writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/collection/range/StringRange", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false);
			return;
		}
		
		writer.writeTypeInsn(Opcodes.NEW, "dyvil/collection/range/SimpleRange");
		writer.writeInsn(Opcodes.DUP);
		this.firstValue.writeExpression(writer);
		this.lastValue.writeExpression(writer);
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
		this.firstValue.toString(prefix, buffer);
		buffer.append(" .. ");
		this.lastValue.toString(prefix, buffer);
	}
}
