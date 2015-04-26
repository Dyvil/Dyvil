package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class LongPattern extends ASTNode implements IPattern
{
	private long	value;
	
	public LongPattern(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return LONG;
	}
	
	@Override
	public IType getType()
	{
		return Types.LONG;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type == Types.LONG)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.LONG) ? new BoxPattern(this, Types.LONG.unboxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.LONG || type.isSuperTypeOf(Types.LONG);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.LLOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_LCMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.LLOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_LCMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
