package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class StringPattern extends ASTNode implements IPattern
{
	private String	value;
	
	public StringPattern(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return STRING;
	}
	
	@Override
	public IType getType()
	{
		return Type.STRING;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		return type == Type.STRING || type.isSuperTypeOf(Type.STRING) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.STRING || type.isSuperTypeOf(Type.STRING);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeLDC(this.value);
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		writer.writeJumpInsn(Opcodes.IFNE, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeLDC(this.value);
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		writer.writeJumpInsn(Opcodes.IFEQ, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}
