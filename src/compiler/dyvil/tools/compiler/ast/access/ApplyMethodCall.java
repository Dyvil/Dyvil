package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	protected IValue resolveCall(MarkerList markers, IContext context)
	{
		IMethod method = ICall.resolveMethod(context, this.instance, Names.apply, this.arguments);
		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return this;
		}
		
		ICall.addResolveMarker(markers, this.position, this.instance, Names.apply, this.arguments);
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
			this.genericData.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
