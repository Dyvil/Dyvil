package dyvil.tools.compiler.ast.expression.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class SubscriptAccess extends AbstractCall
{
	public SubscriptAccess(SourcePosition position)
	{
		this.position = position;
	}

	public SubscriptAccess(SourcePosition position, IValue instance)
	{
		this.position = position;
		this.receiver = instance;
	}

	public SubscriptAccess(SourcePosition position, IValue instance, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}

	@Override
	public int valueTag()
	{
		return SUBSCRIPT_GET;
	}

	@Override
	public Name getName()
	{
		return Names.subscript;
	}

	@Override
	protected Name getReferenceName()
	{
		return Names.subscript_$amp;
	}

	@Override
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return new SubscriptAssignment(this.position.to(position), this.receiver, this.arguments, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, SourcePosition position, MarkerList markers, IContext context,
		                                  SideEffectHelper helper)
	{
		// x[y...] op= z
		// -> x[y...] = x[y...].op(z)
		// -> x.subscript_=(y..., x.subscript(y...).op(z))

		final IValue subscriptReceiver = helper.processValue(this.receiver);
		final ArgumentList subscriptArguments = helper.processArguments(this.arguments);

		this.receiver = subscriptReceiver;
		this.arguments = subscriptArguments;
		return new SubscriptAssignment(position, subscriptReceiver, subscriptArguments, rhs)
			       .resolveCall(markers, context, true);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}

		Formatting.appendSeparator(buffer, "method.subscript.open_bracket", '[');

		int count = this.arguments.size();
		if (count > 0)
		{
			this.arguments.get(0, null).toString(prefix, buffer);
			for (int i = 1; i < count; i++)
			{
				Formatting.appendSeparator(buffer, "method.subscript.separator", ',');
				this.arguments.get(i, null).toString(prefix, buffer);
			}
		}

		if (Formatting.getBoolean("method.subscript.close_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
