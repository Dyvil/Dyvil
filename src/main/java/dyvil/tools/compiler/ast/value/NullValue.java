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

public class NullValue extends ASTNode implements IValue
{
	public NullValue()
	{
	}
	
	public NullValue(ICodePosition position)
	{
		this.position = position;
	}
	
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
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public NullValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitInsn(Opcodes.ACONST_NULL, Type.NONE);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitJumpInsn(Opcodes.GOTO, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
