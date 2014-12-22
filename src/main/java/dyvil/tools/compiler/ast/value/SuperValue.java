package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SuperValue extends ASTNode implements IValue
{
	public Type	type;
	
	public SuperValue(ICodePosition position, Type type)
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
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public SuperValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			if (context.isStatic())
			{
				state.addMarker(new SemanticError(this.position, "'super' cannot be accessed in a static context"));
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("super");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitIntInsn(Opcodes.ALOAD, 0);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}
}
