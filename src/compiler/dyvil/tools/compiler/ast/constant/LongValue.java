package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongValue extends ASTNode implements INumericValue
{
	public long	value;
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	public LongValue(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return LONG;
	}
	
	@Override
	public Type getType()
	{
		return Type.LONG;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Type.LONG ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.LONG;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type == Type.LONG ? 3 : 0;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.value;
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
	public Long toObject()
	{
		return Long.valueOf(this.value);
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
		writer.visitInsn(Opcodes.LRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.position == null)
		{
			buffer.append(this.value).append('L');
			return;
		}
		
		buffer.append(this.position.getText());
	}
}
