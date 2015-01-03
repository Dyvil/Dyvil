package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ThisValue extends ASTNode implements IValue
{
	public IType	type;
	
	public ThisValue(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public ThisValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.CHECK)
		{
			if (context.isStatic())
			{
				state.addMarker(new SemanticError(this.position, "'this' cannot be accessed in a static context"));
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("this");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitIntInsn(Opcodes.ALOAD, 0);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		// TODO
	}
	
	@Override
	public void writeJump(MethodWriter visitor, Label label)
	{
	}
}
