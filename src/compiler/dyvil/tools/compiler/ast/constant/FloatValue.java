package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.boxed.BoxedValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FloatValue extends ASTNode implements INumericValue
{
	public float	value;
	
	public FloatValue(float value)
	{
		this.value = value;
	}
	
	public FloatValue(ICodePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return FLOAT;
	}
	
	@Override
	public Type getType()
	{
		return Type.FLOAT;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.FLOAT)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.FLOAT) ? new BoxedValue(this, Type.FLOAT.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.FLOAT || type.isSuperTypeOf(Type.FLOAT);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.FLOAT)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.FLOAT))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public Float toObject()
	{
		return Float.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
		writer.visitInsn(Opcodes.FRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.position == null)
		{
			buffer.append(this.value).append('F');
			return;
		}
		
		buffer.append(this.position.getText());
	}
}
