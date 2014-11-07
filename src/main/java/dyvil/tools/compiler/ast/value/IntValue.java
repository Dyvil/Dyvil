package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class IntValue extends ASTNode implements IValue
{
	public int	value;
	
	public IntValue(String value)
	{
		this.value = Integer.parseInt(value);
	}
	
	public IntValue(String value, int radix)
	{
		this.value = Integer.parseInt(value);
	}
	
	public IntValue(int value)
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
		return Type.INT;
	}
	
	@Override
	public IntValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		visitor.visitLdcInsn(Integer.valueOf(this.value));
	}
}
