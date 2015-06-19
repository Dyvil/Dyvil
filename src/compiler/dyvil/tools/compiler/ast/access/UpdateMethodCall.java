package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class UpdateMethodCall extends AbstractCall
{
	public UpdateMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return UPDATE_METHOD_CALL;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.addLastValue(Name.update, value);
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setArguments(IArguments arguments)
	{
	}
	
	@Override
	public IArguments getArguments()
	{
		return null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		this.arguments.resolve(markers, context);
		
		IMethod method = ICall.resolveMethod(context, this.instance, Name.update, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		Marker marker = markers.create(this.position, "resolve.method", "update");
		marker.addInfo("Callee Type: " + this.instance.getType());
		if (!this.arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			this.arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}
		
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
