package dyvil.tools.compiler.ast.bytecode;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Bytecode extends ASTNode implements IValue
{
	private List<Instruction>	instructions	= new ArrayList();
	
	public Bytecode(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public Type getType()
	{
		return Type.VOID;
	}

	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		for (Instruction i : this.instructions)
		{
			i.applyState(state, context);
		}
		
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer)
	{
		for (Instruction i : this.instructions)
		{
			i.write(writer);
		}
	}

	@Override
	public void writeStatement(MethodWriter writer)
	{
		for (Instruction i : this.instructions)
		{
			i.write(writer);
		}
	}

	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instructions.isEmpty())
		{
			buffer.append('@').append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append('\n').append(prefix).append("@{\n");
			for (Instruction i : this.instructions)
			{
				buffer.append(prefix).append(Formatting.Method.indent);
				i.toString("", buffer);
				buffer.append(";\n");
			}
			buffer.append(prefix).append('}');
		}
	}
}
