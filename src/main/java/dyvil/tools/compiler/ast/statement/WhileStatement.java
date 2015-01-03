package dyvil.tools.compiler.ast.statement;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class WhileStatement extends ASTNode implements IStatement
{
	private IValue	condition;
	private IValue	then;
	
	public WhileStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.then.getType();
	}
	
	@Override
	public WhileStatement applyState(CompilerState state, IContext context)
	{
		this.condition = this.condition.applyState(state, null);
		this.then = this.then.applyState(state, context);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.whileStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.whileEnd);
		
		if (this.then != null)
		{
			this.then.toString(prefix, buffer);
		}
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeExpression(writer);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		writer.visitLabel(start);
		this.condition.writeJump(writer, end);
		this.then.writeExpression(writer);
		writer.visitJumpInsn(Opcodes.GOTO, start);
		writer.visitLabel(end);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeStatement(writer);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		writer.visitLabel(start);
		this.condition.writeJump(writer, end);
		this.then.writeStatement(writer);
		writer.visitJumpInsn(Opcodes.GOTO, start);
		writer.visitLabel(end);
	}
}
