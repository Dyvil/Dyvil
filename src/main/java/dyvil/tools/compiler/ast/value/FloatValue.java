package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class FloatValue extends ASTObject implements IValue
{
	public float	value;
	
	public FloatValue(String value)
	{
		this.value = Float.parseFloat(value);
	}
	
	public FloatValue(float value)
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
		return Type.FLOAT;
	}
	
	@Override
	public FloatValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		visitor.visitLdcInsn(Float.valueOf(this.value));
	}
}
