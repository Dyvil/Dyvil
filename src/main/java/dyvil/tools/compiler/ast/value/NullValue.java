package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class NullValue extends ASTNode implements IValue
{
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return Type.NONE;
	}
	
	@Override
	public NullValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		visitor.visitInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeJump(MethodVisitor visitor, Label label)
	{
		visitor.visitJumpInsn(Opcodes.GOTO, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
