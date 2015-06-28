package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ApplyMethodCall extends AbstractCall
{
	public ApplyMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public ApplyMethodCall(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
		this.arguments = arguments;
	}
	
	@Override
	public int valueTag()
	{
		return APPLY_CALL;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		this.arguments.resolve(markers, context);
		
		IMethod method = ICall.resolveMethod(context, this.instance, Name.apply, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		ICall.addResolveMarker(markers, this.position, this.instance, Name.apply, this.arguments);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		
		if (this.genericData != null)
		{
			if (this.instance != null || this.instance.valueTag() != FIELD_ACCESS)
			{
				buffer.append(".apply");
			}
			this.genericData.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
