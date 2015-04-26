package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class IntPattern extends ASTNode implements IPattern
{
	private int	value;
	
	public IntPattern(ICodePosition position, int value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return INT;
	}
	
	@Override
	public IType getType()
	{
		return Types.INT;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.INT || type.isSuperTypeOf(Types.INT);
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type == Types.INT)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.INT) ? new BoxPattern(this, Types.INT.unboxMethod) : null;
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
