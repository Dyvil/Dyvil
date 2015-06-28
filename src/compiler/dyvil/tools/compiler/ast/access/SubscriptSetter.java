package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SubscriptSetter extends AbstractCall
{
	public SubscriptSetter(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
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
		this.arguments = this.arguments.addLastValue(Name.update, value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.arguments.getLastValue();
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		this.arguments.resolve(markers, context);
		
		IMethod m = ICall.resolveMethod(context, instance, Name.subscript_$eq, arguments);
		if (m != null)
		{
			this.method = m;
			this.checkArguments(markers, context);
			return this;
		}
		
		ICall.addResolveMarker(markers, position, instance, Name.subscript_$eq, arguments);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		
		if (this.arguments instanceof ArgumentList)
		{
			buffer.append('[');
			int len = this.arguments.size() - 1;
			
			this.arguments.getValue(0, null).toString(prefix, buffer);
			for (int i = 1; i < len; i++)
			{
				buffer.append(", ");
				this.arguments.getValue(i, null).toString(prefix, buffer);
			}
			buffer.append(']');
			buffer.append(Formatting.Field.keyValueSeperator);
			this.arguments.getValue(len, null).toString(prefix, buffer);
		}
		else
		{
			this.arguments.toString(prefix, buffer);
		}
	}
}
