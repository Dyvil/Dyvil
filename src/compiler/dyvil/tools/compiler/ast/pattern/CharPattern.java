package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CharPattern extends ASTNode implements IPattern
{
	private char	value;
	
	public CharPattern(ICodePosition position, char value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return CHAR;
	}
	
	@Override
	public IType getType()
	{
		return Type.CHAR;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type == Type.CHAR)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.CHAR) ? new BoxPattern(this, Type.CHAR.unboxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.CHAR || type.isSuperTypeOf(Type.CHAR);
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('\'').append(this.value).append('\'');
	}
}
