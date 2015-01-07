package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FloatValue extends ASTNode implements IConstantValue
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
	public Type getType()
	{
		return Type.FLOAT;
	}
	
	@Override
	public int getValueType()
	{
		return FLOAT;
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
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitLdcInsn(this.value);
		visitor.visitJumpInsn(Opcodes.IFNE, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}
