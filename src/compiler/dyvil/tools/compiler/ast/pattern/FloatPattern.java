package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FloatPattern extends ASTNode implements IPattern
{
	private float	value;
	
	public FloatPattern(ICodePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return FLOAT;
	}
	
	@Override
	public IType getType()
	{
		return Type.FLOAT;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.FLOAT || type.isSuperTypeOf(Type.FLOAT);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
		writer.writeVarInsn(Opcodes.FLOAD, varIndex);
		writer.writeLDC(this.value);
		writer.writeFrameJump(Opcodes.IF_FCMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}
