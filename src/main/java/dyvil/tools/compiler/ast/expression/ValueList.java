package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ValueList extends ASTNode implements IValue, IValueList
{
	protected List<IValue>	values	= new ArrayList(3);
	
	protected boolean		isArray;
	protected Type			requiredType;
	
	public ValueList(ICodePosition position)
	{
		this.position = position;
	}
	
	public ValueList(ICodePosition position, boolean array)
	{
		this.position = position;
		this.isArray = array;
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
	
	@Override
	public Type getType()
	{
		if (this.values == null || this.values.isEmpty())
		{
			return Type.VOID;
		}
		
		if (this.requiredType != null)
		{
			return this.requiredType;
		}
		
		int len = this.values.size();
		Type t = this.values.get(0).getType();
		for (int i = 1; i < len; i++)
		{
			IValue v = this.values.get(i);
			t = Type.findCommonSuperType(t, v.getType());
		}
		t = t.clone();
		t.arrayDimensions++;
		return this.requiredType = t;
	}
	
	@Override
	public boolean requireType(Type type)
	{
		if (type.arrayDimensions > 0)
		{
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
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.CHECK)
		{
			Type type = this.requiredType;
			type.arrayDimensions--;
			for (IValue value : this.values)
			{
				if (!value.requireType(type))
				{
					state.addMarker(new SemanticError(value.getPosition(), "The array value is incompatible with the required type " + type));
				}
				
				value.applyState(state, context);
			}
			type.arrayDimensions++;
		}
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.applyState(state, context);
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
			Type t = this.getType();
			int len = this.values.size();
			int opcode = t.getArrayStoreOpcode();
			Object frame = t.getFrameType();
			
			writer.visitLdcInsn(len);
			writer.visitTypeInsn(Opcodes.ANEWARRAY, t);
			
			for (int i = 0; i < len; i++)
			{
				writer.visitInsn(Opcodes.DUP, frame);
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
		for (IValue ivalue : this.values)
		{
			ivalue.writeExpression(writer);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
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
				buffer.append("{ ");
				this.values.get(0).toString("", buffer);
				buffer.append(" }");
			}
			else
			{
				buffer.append('\n').append(prefix).append('{').append('\n');
				for (IValue value : this.values)
				{
					buffer.append(prefix).append(Formatting.Method.indent);
					value.toString(prefix + Formatting.Method.indent, buffer);
					buffer.append(";\n");
				}
				buffer.append(prefix).append('}');
			}
		}
	}
}
