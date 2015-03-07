package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.boxed.BoxedValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IntValue extends ASTNode implements INumericValue
{
	public int	value;
	
	public IntValue(int value)
	{
		this.value = value;
	}
	
	public IntValue(ICodePosition position, int value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return INT;
	}
	
	@Override
	public Type getType()
	{
		return Type.INT;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.INT)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.INT) ? new BoxedValue(this, Type.INT.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.INT || type.isSuperTypeOf(Type.INT);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.INT)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.INT))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public long longValue()
	{
		return this.value;
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
	public Integer toObject()
	{
		return Integer.valueOf(this.value);
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
		writer.visitInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
