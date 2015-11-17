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

public class UpdateMethodCall extends AbstractCall implements IValueConsumer
{
	public UpdateMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public UpdateMethodCall(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = arguments;
	}
	
	@Override
	public int valueTag()
	{
		return UPDATE_CALL;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(Names.update, value);
	}
	
	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		IMethod method = ICall.resolveMethod(context, this.receiver, Names.update, this.arguments);
		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return this;
		}
		
		return null;
	}
	
	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
		ICall.addResolveMarker(markers, this.position, this.receiver, Names.update, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		
		if (this.arguments instanceof ArgumentList)
		{
			buffer.append(Formatting.Method.parametersStart);
			int len = this.arguments.size() - 1;
			
			this.arguments.getValue(0, null).toString(prefix, buffer);
			for (int i = 1; i < len; i++)
			{
				buffer.append(Formatting.Method.parameterSeperator);
				this.arguments.getValue(i, null).toString(prefix, buffer);
			}
			buffer.append(Formatting.Method.parametersEnd);
			buffer.append(Formatting.Field.keyValueSeperator);
			this.arguments.getValue(len, null).toString(prefix, buffer);
		}
		else
		{
			this.arguments.toString(prefix, buffer);
		}
	}
}
