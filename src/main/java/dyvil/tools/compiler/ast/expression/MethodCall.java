package dyvil.tools.compiler.ast.expression;

import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class MethodCall implements IValue, INamed, IValueList
{
	protected IValue		instance;
	protected String		name;
	protected List<IValue>	arguments;
	
	public IMethod			descriptor;
	
	@Override
	public IValue fold()
	{
		// TODO Constant Folding
		return this;
	}
	
	@Override
	public Type getType()
	{
		return this.descriptor.getType();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('.').append(this.name);
		buffer.append(Formatting.Method.parametersStart);
		// TODO Args
		buffer.append(Formatting.Method.parametersEnd);
	}
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
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
