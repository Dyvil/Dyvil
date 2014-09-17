package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.value.IValue;

public abstract class Call extends ASTObject implements IValue, IValueList
{
	protected List<IValue>	arguments = new ArrayList(3);
	
	protected boolean		isSugarCall;
	
	public IMethod			descriptor;
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Call applyState(CompilerState state)
	{
		this.arguments.replaceAll(a -> a.applyState(state));
		return this;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	public void setSugarCall(boolean isSugarCall)
	{
		this.isSugarCall = isSugarCall;
	}
	
	public boolean isSugarCall()
	{
		return this.isSugarCall;
	}
}
