package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

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
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.INT);
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		}
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		}
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
