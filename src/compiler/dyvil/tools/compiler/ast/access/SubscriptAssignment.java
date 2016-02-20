package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class SubscriptAssignment extends AbstractCall implements IValueConsumer
{
	public SubscriptAssignment(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}
	
	@Override
	public int valueTag()
	{
		return SUBSCRIPT_SET;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(Names.update, value);
	}
	
	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		IMethod m = ICall.resolveMethod(context, this.receiver, Names.subscript_$eq, this.arguments);
		if (m != null)
		{
			this.method = m;
			this.checkArguments(markers, context);
			return this;
		}
		
		return null;
	}
	
	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
		ICall.addResolveMarker(markers, this.position, this.receiver, Names.subscript_$eq, this.arguments);
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

		Formatting.appendSeparator(buffer, "method.subscript.open_bracket", '[');

		int count = this.arguments.size() - 1;
		this.arguments.getValue(0, null).toString(prefix, buffer);
		for (int i = 1; i < count; i++)
		{
			Formatting.appendSeparator(buffer, "method.subscript.separator", ',');
			this.arguments.getValue(i, null).toString(prefix, buffer);
		}

		if (Formatting.getBoolean("method.subscript.close_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');

		Formatting.appendSeparator(buffer, "field.assignment", '=');

		this.arguments.getLastValue().toString(prefix, buffer);
	}
}
