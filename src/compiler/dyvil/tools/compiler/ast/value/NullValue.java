package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class NullValue extends ASTNode implements IConstantValue
{
	public NullValue()
	{
	}
	
	public NullValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public Type getType()
	{
		return Type.NONE;
	}
	
	@Override
	public int getValueType()
	{
		return NULL;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitInsn(Opcodes.ACONST_NULL, Type.NONE);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
