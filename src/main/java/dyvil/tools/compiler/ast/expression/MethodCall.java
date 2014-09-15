package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class MethodCall extends Call implements INamed
{
	protected IValue		instance;
	protected String		name;
	
	@Override
	public Type getType()
	{
		return this.descriptor.getType();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isSugarCall)
		{
			buffer.append(' ').append(this.name);
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			buffer.append('.').append(this.name);
			buffer.append(Formatting.Method.parametersStart);
			// TODO Args
			buffer.append(Formatting.Method.parametersEnd);
		}
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
}
