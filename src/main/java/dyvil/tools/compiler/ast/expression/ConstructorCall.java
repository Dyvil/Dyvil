package dyvil.tools.compiler.ast.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class ConstructorCall implements IValue, ITyped, IValueList
{
	protected Type			type;
	protected List<IValue>	arguments	= new ArrayList();
	
	public IMethod			descriptor;
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		buffer.append(Formatting.Method.parametersStart);
		// TODO Args
		buffer.append(Formatting.Method.parametersEnd);
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
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
}
