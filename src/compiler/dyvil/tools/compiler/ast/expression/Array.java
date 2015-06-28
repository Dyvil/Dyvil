package dyvil.tools.compiler.ast.expression;

import java.util.Iterator;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class Array extends ASTNode implements IValue, IValueList
{
	public static final IClass	ARRAY_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("ArrayConvertible");
	
	protected IValue[]			values;
	protected int				valueCount;
	
	protected IType				requiredType;
	protected IType				elementType;
	
	public Array()
	{
		this.values = new IValue[3];
	}
	
	public Array(ICodePosition position)
	{
		this.values = new IValue[3];
		this.position = position;
	}
	
	public Array(IValue[] values, int valueCount)
	{
		this.values = values;
		this.valueCount = valueCount;
	}
	
	@Override
	public int valueTag()
	{
		return ARRAY;
	}
	
	@Override
	public boolean isConstant()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isConstant())
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
	public IType getType()
	{
		if (this.requiredType != null)
		{
			return this.requiredType;
		}
		
		int len = this.valueCount;
		if (len == 0)
		{
			this.elementType = Types.ANY;
			return this.requiredType = new ArrayType(Types.ANY);
		}
		
		IType t = this.values[0].getType();
		for (int i = 1; i < len; i++)
		{
			IType t1 = this.values[i].getType();
			t = Types.findCommonSuperType(t, t1);
			if (t == null)
			{
				t = Types.ANY;
				break;
			}
		}
		
		this.elementType = t;
		
		if (t.getTheClass() == Types.TUPLE2_CLASS)
		{
			GenericType type = new GenericType(Types.MAP_CLASS);
			type.typeArgumentCount = 2;
			
			switch (t.typeTag())
			{
			case IType.GENERIC:
			case IType.TUPLE:
				ITypeList t1 = (ITypeList) t;
				type.typeArguments[0] = t1.getType(0);
				type.typeArguments[1] = t1.getType(1);
				break;
			default:
				type.typeArguments[0] = type.typeArguments[1] = Types.ANY;
			}
			
			return this.requiredType = type;
		}
		return this.requiredType = new ArrayType(t);
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (!type.isArrayType())
		{
			IClass iclass = type.getTheClass();
			if (iclass == Types.OBJECT_CLASS || iclass == null)
			{
				return this;
			}
			if (iclass.getAnnotation(ARRAY_CONVERTIBLE) != null)
			{
				return new LiteralExpression(type, this);
			}
			
			return null;
		}
		
		// If the type is an array type, get it's element type
		IType type1 = type.getElementType();
		
		// Check for every value if it is the element type
		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isType(type1))
			{
				// If not, this is not the type
				return null;
			}
		}
		
		this.elementType = type1;
		this.requiredType = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (!type.isArrayType())
		{
			IClass iclass = type.getTheClass();
			return iclass == Types.OBJECT_CLASS || iclass.getAnnotation(ARRAY_CONVERTIBLE) != null;
		}
		
		// Skip getting the element type if this is an empty array
		if (this.valueCount == 0)
		{
			return true;
		}
		
		// If the type is an array type, get it's element type
		IType type1 = type.getElementType();
		
		// Check for every value if it is the element type
		for (int i = 0; i < this.valueCount; i++)
		{
			if (!this.values[i].isType(type1))
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
			IClass iclass = type.getTheClass();
			return iclass == Types.OBJECT_CLASS || iclass.getAnnotation(ARRAY_CONVERTIBLE) != null ? 2 : 0;
		}
		
		// Skip getting the element type if this is an empty array
		if (this.valueCount == 0)
		{
			return 3;
		}
		
		// If the type is an array type, get it's element type
		IType type1 = type.getElementType();
		int total = 0;
		
		// Get the type match for every value in the array
		for (int i = 0; i < this.valueCount; i++)
		{
			int m = this.values[i].getTypeMatch(type1);
			if (m == 0)
			{
				// If the type match for one value is zero, return 0
				return 0;
			}
			total += m;
		}
		
		// Divide by the count
		return 1 + total / this.valueCount;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values);
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
		int i = this.valueCount++;
		System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
		this.values[index] = value;
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
		if (this.elementType == null)
		{
			if (this.valueCount == 0)
			{
				markers.add(this.position, "array.empty");
				return;
			}
			
			this.getType();
			
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].checkTypes(markers, context);
			}
		}
		
		IType type = this.elementType;
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			IValue value1 = value.withType(type);
			
			if (value1 == null)
			{
				Marker marker = markers.create(value.getPosition(), "array.element.type");
				marker.addInfo("Array Type: " + this.requiredType);
				marker.addInfo("Array Element Type: " + value.getType());
			}
			else
			{
				value = value1;
				this.values[i] = value1;
			}
			value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		IType type = this.elementType;
		int opcode = type.getArrayStoreOpcode();
		
		writer.writeLDC(this.valueCount);
		writer.writeNewArray(type, 1);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			IValue value = this.values[i];
			writer.writeLDC(i);
			value.writeExpression(writer);
			writer.writeInsn(opcode);
		}
		
		if (this.requiredType.getTheClass() == Types.MAP_CLASS)
		{
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/lang/Map", "apply", "([Ldyvil/tuple/Tuple2;)Ldyvil/collection/ImmutableMap;", true);
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
		if (this.valueCount == 0)
		{
			buffer.append(Formatting.Expression.emptyArray);
		}
		else
		{
			buffer.append(Formatting.Expression.arrayStart);
			Util.astToString(prefix, this.values, this.valueCount, Formatting.Expression.arraySeperator, buffer);
			buffer.append(Formatting.Expression.arrayEnd);
		}
	}
}
