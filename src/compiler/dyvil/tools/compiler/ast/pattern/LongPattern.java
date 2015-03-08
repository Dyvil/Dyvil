package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongPattern extends ASTNode implements IPattern
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
		return Type.LONG;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.LONG || type.isSuperTypeOf(Type.LONG);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label elseLabel)
	{
		writer.writeLDC(this.value);
		writer.writeFrameJump(Opcodes.IF_LCMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
