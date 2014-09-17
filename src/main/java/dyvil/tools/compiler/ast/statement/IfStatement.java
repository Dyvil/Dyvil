package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BooleanValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class IfStatement extends ASTObject implements IStatement
{
	private IValue	condition;
	private IValue	then;
	private IValue	elseThen;
	
	public IfStatement()
	{}
	
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
	public IValue applyState(CompilerState state)
	{
		this.condition = this.condition.applyState(state);
		this.then = this.then.applyState(state);
		this.elseThen = this.elseThen.applyState(state);
		
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
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.ifStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.ifEnd);
		this.then.toString(prefix, buffer);
		
		if (this.elseThen != null)
		{
			buffer.append(Formatting.Statements.ifElse);
			this.elseThen.toString(prefix, buffer);
		}
	}
}
