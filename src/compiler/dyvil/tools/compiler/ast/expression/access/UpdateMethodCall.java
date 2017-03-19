package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class UpdateMethodCall extends AbstractCall implements IValueConsumer
{
	public UpdateMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public UpdateMethodCall(ICodePosition position, IValue instance, IArguments arguments, IValue rhs)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments.withLastValue(Names.eq, rhs);
	}
	
	@Override
	public int valueTag()
	{
		return UPDATE_CALL;
	}

	@Override
	public Name getName()
	{
		return Names.update;
	}

	@Override
	protected Name getReferenceName()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(Names.update, value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		
		if (!(this.arguments instanceof ArgumentList))
		{
			this.arguments.toString(prefix, buffer);
		}

		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		int count = this.arguments.size() - 1;
		this.arguments.getValue(0, null).toString(prefix, buffer);
		for (int i = 1; i < count; i++)
		{
			Formatting.appendSeparator(buffer, "parameters.separator", ',');
			this.arguments.getValue(i, null).toString(prefix, buffer);
		}

		if (Formatting.getBoolean("parameters.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		Formatting.appendSeparator(buffer, "field.assignment", '=');

		this.arguments.getLastValue().toString(prefix, buffer);
	}
}
