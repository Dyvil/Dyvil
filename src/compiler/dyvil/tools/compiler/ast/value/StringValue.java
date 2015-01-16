package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StringValue extends ASTNode implements IConstantValue
{
	public String	value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public StringValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public Type getType()
	{
		return Type.STRING;
	}
	
	@Override
	public int getValueType()
	{
		return STRING;
	}
	
	@Override
	public String toObject()
	{
		return this.value;
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
		buffer.append('"').append(this.value).append('"');
	}
}
