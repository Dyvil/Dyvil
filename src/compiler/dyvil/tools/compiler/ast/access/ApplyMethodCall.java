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
		this.receiver = instance;
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
		// Merge Applied Statement Lists if a non-curried method version is
		// available. Doesn't works with multiple applied statement lists.
		// with(x...) { statements }
		// -> with(x..., { statements })
		if (this.arguments.getClass() == SingleArgument.class && this.receiver instanceof ICall)
		{
			ICall call = (ICall) this.receiver;
			IValue argument = this.arguments.getFirstValue();
			
			if (argument instanceof AppliedStatementList)
			{
				argument = argument.resolve(markers, context);
				
				IArguments oldArgs = call.getArguments();
				call.resolveReceiver(markers, context);
				call.resolveArguments(markers, context);
				
				call.setArguments(oldArgs.withLastValue(Names.apply, argument));
				
				IValue resolvedCall = call.resolveCall(markers, context);
				if (resolvedCall != null)
				{
					return resolvedCall;
				}
				
				// Revert
				call.setArguments(oldArgs);
				
				this.receiver = call.resolveCall(markers, context);
				resolvedCall = this.resolveCall(markers, context);
				if (resolvedCall != null)
				{
					return resolvedCall;
				}
				
				this.reportResolve(markers, context);
				return this;
			}
		}
		
		return super.resolve(markers, context);
	}
	
	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		IMethod method = ICall.resolveMethod(context, this.receiver, Names.apply, this.arguments);
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
		ICall.addResolveMarker(markers, this.position, this.receiver, Names.apply, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		
		if (this.genericData != null)
		{
			this.genericData.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
