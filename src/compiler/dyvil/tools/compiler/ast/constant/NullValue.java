package dyvil.tools.compiler.ast.constant;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
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
	public int getValueType()
	{
		return NULL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return Type.NONE;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type.isPrimitive() ? null : this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type.isPrimitive() ? 0 : 2;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitInsn(Opcodes.ACONST_NULL);
		writer.visitInsn(Opcodes.RETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
