package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Call extends ASTObject implements IValue, IValueList, IAccess
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
	public IAccess applyState(CompilerState state, IContext context)
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
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	public Type[] getTypes()
	{
		int len = this.arguments.size();
		Type[] types = new Type[len];
		for (int i = 0; i < len; i++)
		{
			IValue arg = this.arguments.get(i);
			if (arg == null)
				return null;
			
			Type t = arg.getType();
			if (t == null)
				return null;
			
			types[i] = t;
		}
		return types;
	}
	
	public void setSugar(boolean sugar)
	{
		this.isSugarCall = sugar;
	}
}
