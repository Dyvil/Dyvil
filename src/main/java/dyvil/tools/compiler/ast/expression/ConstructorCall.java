package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;

public class ConstructorCall extends Call implements ITyped
{
	protected Type	type;
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			buffer.append(Formatting.Method.parametersStart);
			// TODO Args
			buffer.append(Formatting.Method.parametersEnd);
		}
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
}
