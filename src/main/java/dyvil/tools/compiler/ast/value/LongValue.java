package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongValue extends ASTNode implements IConstantValue
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
	public Type getType()
	{
		return Type.LONG;
	}
	
	@Override
	public int getValueType()
	{
		return LONG;
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
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
