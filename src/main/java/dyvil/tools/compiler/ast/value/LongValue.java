package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;

public class LongValue extends ASTNode implements IValue
{
	public long	value;
	
	public LongValue(String value)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(String value, int radix)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return Type.LONG;
	}
	
	@Override
	public LongValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(Long.valueOf(this.value));
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitLdcInsn(Long.valueOf(this.value));
		visitor.visitLdcInsn(Long.valueOf(0));
		visitor.visitJumpInsn(Opcodes.IFNE, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
