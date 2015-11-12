package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.statement.AppliedStatementList;
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
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.arguments.getClass() == SingleArgument.class && this.instance instanceof AbstractCall)
		{
			ICall ac = (ICall) this.instance;
			IValue argument = this.arguments.getFirstValue();
			
			if (argument instanceof AppliedStatementList)
			{
				argument = argument.resolve(markers, context);
				
				IArguments oldArgs = ac.getArguments();
				ac.resolveArguments(markers, context);
				
				ac.setArguments(oldArgs.withLastValue(Names.apply, argument));
				
				IValue call = ac.resolveCall(markers, context);
				if (call != null)
				{
					return call;
				}
				
				ac.setArguments(oldArgs);
				
				this.instance = ac.resolveCall(markers, context);
				return this.resolveCall(markers, context);
			}
		}
		
		return super.resolve(markers, context);
	}
	
	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
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
