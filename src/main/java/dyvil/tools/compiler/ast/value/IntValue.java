package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IntValue extends ASTNode implements IValue
{
	public int	value;
	
	public IntValue(int value)
	{
		this.value = value;
	}
	
	public IntValue(ICodePosition position, int value)
	{
		this.position = position;
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
		return Type.INT;
	}
	
	@Override
	public Integer toObject()
	{
		return Integer.valueOf(this.value);
	}
	
	@Override
	public IntValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitLdcInsn(this.value);
		visitor.visitJumpInsn(Opcodes.IFNE, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
