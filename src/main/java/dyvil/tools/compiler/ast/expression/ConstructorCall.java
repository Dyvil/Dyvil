package dyvil.tools.compiler.ast.expression;

import java.util.Iterator;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
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
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else if (!this.arguments.isEmpty())
		{
			// TODO Special seperators, named arguments
			buffer.append(Formatting.Method.parametersStart);
			Iterator<IValue> iterator = this.arguments.iterator();
			while (true)
			{
				IValue value = iterator.next();
				value.toString("", buffer);
				
				if (iterator.hasNext())
				{
					buffer.append(Formatting.Method.parametersEnd);
				}
				else
				{
					break;
				}
			}
			buffer.append(Formatting.Method.parametersEnd);
		}
		else
		{
			buffer.append(Formatting.Method.emptyParameters);
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
