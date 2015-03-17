package dyvil.tools.compiler.ast.value;

import java.util.Iterator;

import dyvil.collections.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ValueList extends ASTNode implements IValue, IValueList
{
	protected IValue[]	values;
	protected int		valueCount;
	
	protected boolean	isArray;
	protected IType		requiredType;
	protected IType		elementType;
	
	public ValueList(ICodePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
	}
	
	public ValueList(ICodePosition position, boolean array)
	{
		this.position = position;
		this.isArray = array;
		this.values = new IValue[3];
	}
	
	public ValueList(ICodePosition position, IValue[] values, IType type, IType elementType)
	{
		this.position = position;
		this.isArray = true;
		this.values = values;
		this.valueCount = values.length;
		this.requiredType = type;
		this.elementType = elementType;
	}
	
	@Override
	public int getValueType()
	{
		return VALUE_LIST;
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
		return this.isArray;
	}
	
	private void generateTypes()
	{
		int len = this.valueCount;
		if (len == 0)
		{
			this.elementType = Type.VOID;
			this.requiredType = Type.VOID;
			return;
		}
		
		IType t = this.values[0].getType();
		for (int i = 1; i < len; i++)
		{
			IType t1 = this.values[i].getType();
			t = Type.findCommonSuperType(t, t1);
			if (t == null)
			{
				this.elementType = Type.VOID;
				this.requiredType = Type.VOID;
				return;
			}
		}
		
		this.elementType = t;
		this.requiredType = t.getArrayType();
	}
	
	@Override
	public IType getType()
	{
		if (this.valueCount == 0)
		{
			return Type.VOID;
		}
		
		if (this.requiredType != null)
		{
			return this.requiredType;
		}
		
		this.generateTypes();
		return this.isArray ? this.requiredType : this.elementType;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this.isType(type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.isArrayType())
		{
			// If the type is an array type, get it's element type
			IType type1 = type.getElementType();
			this.isArray = true;
			this.elementType = type1;
			this.requiredType = type;
			
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
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.isArrayType())
		{
			// If the type is an array type, get it's element type
			IType type1 = type.getElementType();
			this.isArray = true;
			this.elementType = type1;
			this.requiredType = type;
			
			// Check for every value if it is the element type
			for (int i = 0; i < this.valueCount; i++)
			{
				if (!this.values[i].isType(type1))
				{
					// If not, this is not the type
					return 1;
				}
			}
			
			return 3;
		}
		return 0;
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
		if (this.valueCount > this.values.length)
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
	public void setArray(boolean array)
	{
		this.isArray = array;
	}
	
	@Override
	public boolean isArray()
	{
		return this.isArray;
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
	public void check(MarkerList markers, IContext context)
	{
		if (this.elementType == null)
		{
			this.getType();
			
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].check(markers, context);
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
			value.check(markers, context);
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
	public void writeExpression(MethodWriter writer)
	{
		if (this.isArray)
		{
			IType type = this.elementType;
			int opcode = type.getArrayStoreOpcode();
			
			writer.writeLDC(this.valueCount);
			writer.writeTypeInsn(Opcodes.ANEWARRAY, type);
			
			for (int i = 0; i < this.valueCount; i++)
			{
				writer.writeInsn(Opcodes.DUP);
				IValue value = this.values[i];
				writer.writeLDC(i);
				value.writeExpression(writer);
				writer.writeInsn(opcode);
			}
			return;
		}
		
		DyvilCompiler.logger.warning("ValueList.writeExpression() - Not an Array");
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.isArray)
		{
			this.writeExpression(writer);
			return;
		}
		
		DyvilCompiler.logger.warning("ValueList.writeStatement() - Not an Array");
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isArray)
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
		else
		{
			if (this.valueCount == 0)
			{
				buffer.append(Formatting.Expression.emptyExpression);
			}
			else if (this.valueCount == 1)
			{
				buffer.append(Formatting.Expression.arrayStart);
				this.values[0].toString("", buffer);
				buffer.append(Formatting.Expression.arrayEnd);
			}
			else
			{
				buffer.append('{').append('\n');
				String prefix1 = prefix + Formatting.Method.indent;
				for (int i = 0; i < this.valueCount; i++)
				{
					buffer.append(prefix1);
					this.values[i].toString(prefix1, buffer);
					buffer.append(";\n");
				}
				buffer.append(prefix).append('}');
			}
		}
	}
}
