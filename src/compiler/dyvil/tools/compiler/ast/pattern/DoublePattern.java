package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class DoublePattern extends ASTNode implements IPattern
{
	private double	value;
	
	public DoublePattern(ICodePosition position, double value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return DOUBLE;
	}
	
	@Override
	public IType getType()
	{
		return Type.DOUBLE;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type == Type.DOUBLE)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.DOUBLE) ? new BoxPattern(this, Type.DOUBLE.unboxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.DOUBLE || type.isSuperTypeOf(Type.DOUBLE);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeVarInsn(Opcodes.DLOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_DCMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeVarInsn(Opcodes.DLOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_DCMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}
