package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;

public class SubscriptAssignment extends AbstractCall
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
		this.arguments = arguments.appended(rhs);
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
