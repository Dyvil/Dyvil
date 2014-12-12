package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BooleanValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IfStatement extends ASTNode implements IStatement
{
	private IValue	condition;
	private IValue	then;
	private IValue	elseThen;
	
	public IfStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public void setElse(IValue elseThen)
	{
		this.elseThen = elseThen;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	public IValue getElse()
	{
		return this.elseThen;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return this.getThen().getType();
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		this.condition = this.condition.applyState(state, context);
		
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (BooleanValue.TRUE.equals(this.condition))
			{
				return this.then;
			}
			if (BooleanValue.FALSE.equals(this.condition))
			{
				return this.elseThen;
			}
		}
		
		this.then = this.then.applyState(state, context);
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.applyState(state, context);
		}
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.elseThen != null)
		{
			Label ifEnd = new Label();
			Label elseEnd = new Label();
			
			this.condition.writeJump(writer, ifEnd);
			this.then.writeExpression(writer);
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			writer.visitLabel(ifEnd);
			this.elseThen.writeExpression(writer);
			writer.visitLabel(elseEnd);
		}
		else
		{
			Label ifEnd = new Label();
			this.condition.writeJump(writer, ifEnd);
			this.then.writeExpression(writer);
			writer.visitLabel(ifEnd);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.elseThen != null)
		{
			Label ifEnd = new Label();
			Label elseEnd = new Label();
			
			this.condition.writeJump(writer, ifEnd);
			this.then.writeStatement(writer);
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			writer.visitLabel(ifEnd);
			this.elseThen.writeStatement(writer);
			writer.visitLabel(elseEnd);
		}
		else
		{
			Label ifEnd = new Label();
			this.condition.writeJump(writer, ifEnd);
			this.then.writeStatement(writer);
			writer.visitLabel(ifEnd);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.ifStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.ifEnd);
		this.then.toString(prefix, buffer);
		buffer.append(';');
		
		if (this.elseThen != null)
		{
			buffer.append(Formatting.Statements.ifElse);
			this.elseThen.toString(prefix, buffer);
		}
	}
}
