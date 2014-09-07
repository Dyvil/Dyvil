package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class IfStatement implements IValue, IValued
{
	private IValue		condition;
	private IValue	then;
	private IValue	elseThen;
	
	public IfStatement()
	{
	}
	
	@Override
	public void setValue(IValue condition)
	{
		this.condition = condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public void setElseThen(IValue elseThen)
	{
		this.elseThen = elseThen;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	public IValue getElseThen()
	{
		return this.elseThen;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(Formatting.Statements.ifStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.ifEnd);
		// TODO Body
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public IValue fold()
	{
		return this;
	}

	@Override
	public Type getType()
	{
		return this.getThen().getType();
	}
}
