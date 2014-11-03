package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class LongValue extends ASTObject implements IValue
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
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}

	@Override
	public void write(MethodVisitor visitor)
	{
		visitor.visitLdcInsn(Long.valueOf(this.value));
	}
}
