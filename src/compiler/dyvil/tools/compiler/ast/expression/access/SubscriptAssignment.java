package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public class SubscriptAssignment extends AbstractCall implements IValueConsumer
{
	public SubscriptAssignment(SourcePosition position, IValue receiver, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	public SubscriptAssignment(SourcePosition position, IValue receiver, ArgumentList arguments, IValue rhs)
	{
		this.position = position;
		this.receiver = receiver;
		this.arguments = arguments.appended(Names.eq, rhs);
	}

	@Override
	public int valueTag()
	{
		return SUBSCRIPT_SET;
	}

	@Override
	public Name getName()
	{
		return Names.subscript_$eq;
	}

	@Override
	protected Name getReferenceName()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.appended(Names.update, value);
	}
	
	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(indent, buffer);
		}

		Formatting.appendSeparator(buffer, "method.subscript.open_bracket", '[');

		final int count = this.arguments.size() - 1;
		this.arguments.appendValue(indent, buffer, 0);
		for (int i = 1; i < count; i++)
		{
			Formatting.appendSeparator(buffer, "method.subscript.separator", ',');
			this.arguments.appendValue(indent, buffer, i);
		}

		Formatting.appendClose(buffer, "method.subscript.close_bracket", ']');

		Formatting.appendSeparator(buffer, "field.assignment", '=');

		this.arguments.getLast().toString(indent, buffer);
	}
}
