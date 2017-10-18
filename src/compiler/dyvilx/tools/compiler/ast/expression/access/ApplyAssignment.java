package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;

public class ApplyAssignment extends AbstractCall implements IValueConsumer
{
	public ApplyAssignment(SourcePosition position)
	{
		this.position = position;
	}

	public ApplyAssignment(SourcePosition position, IValue instance, ArgumentList arguments, IValue rhs)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments.appended(Names.eq, rhs);
	}

	@Override
	public int valueTag()
	{
		return UPDATE_CALL;
	}

	@Override
	public Name getName()
	{
		return Names.apply_$eq;
	}

	@Override
	protected Name getReferenceName()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.appended(Names.newValue, value);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(indent, buffer);
		}

		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		final int count = this.arguments.size() - 1;
		this.arguments.appendValue(indent, buffer, 0);
		for (int i = 1; i < count; i++)
		{
			Formatting.appendSeparator(buffer, "parameters.separator", ',');
			this.arguments.appendValue(indent, buffer, i);
		}

		Formatting.appendClose(buffer, "parameters.close_paren", ')');

		Formatting.appendSeparator(buffer, "field.assignment", '=');

		this.arguments.getLast().toString(indent, buffer);
	}
}
