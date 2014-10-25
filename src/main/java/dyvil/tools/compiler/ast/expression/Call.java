package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Call extends ASTObject implements IValue, IValueList
{
	protected List<IValue>	arguments	= new ArrayList(3);
	
	protected boolean		isSugarCall;
	
	public IMethod			method;
	
	public Call(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		this.arguments.replaceAll(a -> a.applyState(state, context));
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
	
	public Type[] getTypes()
	{
		int len = this.arguments.size();
		Type[] types = new Type[len];
		for (int i = 0; i < len; i++)
		{
			IValue arg = this.arguments.get(i);
			if (arg != null)
			{
				types[i] = arg.getType();
			}
		}
		return types;
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
