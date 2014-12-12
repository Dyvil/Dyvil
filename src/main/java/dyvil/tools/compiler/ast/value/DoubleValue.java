package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;

public class DoubleValue extends ASTNode implements IValue
{
	public double	value;
	
	public DoubleValue(String value)
	{
		this.value = Double.parseDouble(value);
	}
	
	public DoubleValue(double value)
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
		return Type.DOUBLE;
	}
	
	@Override
	public DoubleValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(Double.valueOf(this.value));
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitLdcInsn(Double.valueOf(this.value));
		visitor.visitLdcInsn(Double.valueOf(0D));
		visitor.visitJumpInsn(Opcodes.IFNE, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}
