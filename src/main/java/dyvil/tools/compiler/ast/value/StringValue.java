package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class StringValue extends ASTNode implements IValue
{
	public String	value;
	
	public StringValue(String value)
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
		return Type.STRING;
	}
	
	@Override
	public StringValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		visitor.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeJump(MethodVisitor visitor, Label label)
	{
		visitor.visitLdcInsn(this.value);
		visitor.visitJumpInsn(Opcodes.IFNONNULL, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}
