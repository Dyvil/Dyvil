package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IStatement;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
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
	public IType getType()
	{
		return this.then == null ? this.condition.getType() : this.then.getType();
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		this.condition = this.condition.applyState(state, context);
		
		if (state == CompilerState.CHECK)
		{
			if (!Type.isSuperType(Type.BOOLEAN, this.condition.getType()))
			{
				state.addMarker(new SemanticError(this.position, "The condition of an if statement has to evaluate to a boolean value."));
			}
		}
		
		if (this.then != null)
		{
			this.then = this.then.applyState(state, context);
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.applyState(state, context);
		}
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeExpression(writer);
		}
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
		if (this.then == null)
		{
			this.condition.writeStatement(writer);
		}
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
		if (this.then != null)
		{
			this.then.toString(prefix, buffer);
		}
		
		if (this.elseThen != null)
		{
			buffer.append(Formatting.Statements.ifElse);
			this.elseThen.toString(prefix, buffer);
		}
	}
}
