package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ValueList extends ASTNode implements IValue, IValueList
{
	public List<IValue>	values;
	
	protected boolean	isArray;
	protected IType		requiredType;
	protected IType		elementType;
	
	public ValueList(ICodePosition position)
	{
		this.position = position;
		this.values = new ArrayList(3);
	}
	
	public ValueList(ICodePosition position, boolean array)
	{
		this.position = position;
		this.isArray = array;
		this.values = new ArrayList(3);
	}
	
	public ValueList(ICodePosition position, List<IValue> values, IType type, IType elementType)
	{
		this.position = position;
		this.isArray = true;
		this.values = values;
		this.requiredType = type;
		this.elementType = elementType;
	}
	
	@Override
	public boolean isConstant()
	{
		for (IValue v : this.values)
		{
			if (!v.isConstant())
			{
				return false;
			}
		}
		return true;
	}
	
	private void generateTypes()
	{
		int len = this.values.size();
		if (len == 0)
		{
			this.elementType = Type.VOID;
			this.requiredType = Type.VOID;
			return;
		}
		
		IType t = this.values.get(0).getType();
		for (int i = 1; i < len; i++)
		{
			IValue v = this.values.get(i);
			IType t1 = v.getType();
			if (t1 == null)
			{
				t = Type.VOID;
				break;
			}
			t = Type.findCommonSuperType(t, t1);
		}
		if (t != null)
		{
			this.elementType = t;
			this.requiredType = t.clone();
			this.requiredType.addArrayDimension();
		}
	}
	
	@Override
	public IType getType()
	{
		if (this.values == null || this.values.isEmpty())
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
	public int getValueType()
	{
		return VALUE_LIST;
	}
	
	public IType getElementType()
	{
		if (this.elementType == null)
		{
			this.generateTypes();
		}
		
		return this.elementType;
	}
	
	@Override
	public boolean requireType(IType type)
	{
		if (type.isArrayType())
		{
			this.isArray = true;
			if (this.requiredType != null)
			{
				return Type.isSuperType(type, this.requiredType);
			}
			this.requiredType = type;
			return true;
		}
		return false;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.values = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.values.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.values;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values.get(index);
	}
	
	public boolean isEmpty()
	{
		return this.values.isEmpty();
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (IValue v : this.values)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.resolve(markers, context);
			if (v1 != v2)
			{
				this.values.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IType type = this.elementType;
		for (IValue v : this.values)
		{
			v.check(markers, context);
			
			if (!v.requireType(type))
			{
				Marker marker = Markers.create(v.getPosition(), "array.element.type");
				marker.addInfo("Array Type: " + type);
				marker.addInfo("Array Element Type" + v.getType());
				markers.add(marker);
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.values.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.isArray)
		{
			IType type = this.getElementType();
			int len = this.values.size();
			int opcode = type.getArrayStoreOpcode();
			
			writer.visitLdcInsn(len);
			writer.visitTypeInsn(Opcodes.ANEWARRAY, type);
			
			for (int i = 0; i < len; i++)
			{
				writer.visitInsn(Opcodes.DUP, type);
				IValue value = this.values.get(i);
				writer.visitLdcInsn(i);
				value.writeExpression(writer);
				writer.visitInsn(opcode, null, 3);
			}
		}
		else
		{
			for (IValue ivalue : this.values)
			{
				ivalue.writeExpression(writer);
			}
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.isArray)
		{
			this.writeExpression(writer);
			return;
		}
		for (IValue ivalue : this.values)
		{
			ivalue.writeExpression(writer);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isArray)
		{
			if (this.values.isEmpty())
			{
				buffer.append(Formatting.Expression.emptyArray);
			}
			else
			{
				buffer.append(Formatting.Expression.arrayStart);
				Util.astToString(this.values, Formatting.Expression.arraySeperator, buffer);
				buffer.append(Formatting.Expression.arrayEnd);
			}
		}
		else
		{
			int len = this.values.size();
			if (len == 0)
			{
				buffer.append(Formatting.Expression.emptyExpression);
			}
			else if (len == 1)
			{
				buffer.append(Formatting.Expression.arrayStart);
				this.values.get(0).toString("", buffer);
				buffer.append(Formatting.Expression.arrayEnd);
			}
			else
			{
				buffer.append('{').append('\n');
				String prefix1 = prefix + Formatting.Method.indent;
				for (IValue value : this.values)
				{
					buffer.append(prefix1);
					value.toString(prefix1, buffer);
					buffer.append(";\n");
				}
				buffer.append(prefix).append('}');
			}
		}
	}
}
