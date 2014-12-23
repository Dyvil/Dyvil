package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CharValue extends ASTNode implements IValue
{
	public char	value;
	
	public CharValue(char value)
	{
		this.value = value;
	}
	
	public CharValue(ICodePosition position, char value)
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
		return Type.CHAR;
	}
	
	@Override
	public Character toObject()
	{
		return Character.valueOf(this.value);
	}
	
	@Override
	public CharValue applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter visitor)
	{
		visitor.visitLdcInsn(Integer.valueOf(this.value));
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
		visitor.visitLdcInsn(Integer.valueOf(this.value));
		visitor.visitLdcInsn(Integer.valueOf(0));
		visitor.visitJumpInsn(Opcodes.IF_ICMPNE, label);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('\'').append(this.value).append('\'');
	}
}
