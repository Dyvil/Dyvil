package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Call extends ASTNode implements IValue, IValueList, IAccess
{
	protected List<IValue>	arguments	= new ArrayList(3);
	public boolean			isSugarCall;
	
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
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	public void setSugar(boolean sugar)
	{
		this.isSugarCall = sugar;
	}
	
	public IType[] getTypes()
	{
		int len = this.arguments.size();
		IType[] types = new Type[len];
		for (int i = 0; i < len; i++)
		{
			IValue arg = this.arguments.get(i);
			if (arg == null)
			{
				return null;
			}
			
			IType t = arg.getType();
			if (t == null)
			{
				return null;
			}
			
			types[i] = t;
		}
		return types;
	}
}
