package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class SubscriptGetter extends AbstractCall
{
	public SubscriptGetter(ICodePosition position)
	{
		this.position = position;
		this.arguments = new ArgumentList();
	}
	
	public SubscriptGetter(ICodePosition position, IValue instance)
	{
		this.position = position;
		this.receiver = instance;
		this.arguments = new ArgumentList();
	}
	
	public SubscriptGetter(ICodePosition position, IValue instance, IArguments arguments)
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
	public ArgumentList getArguments()
	{
		return (ArgumentList) this.arguments;
	}
	
	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		return null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			if (this.receiver.valueTag() == FIELD_ACCESS)
			{
				FieldAccess fa = (FieldAccess) this.receiver;
				if (fa.receiver != null)
				{
					fa.receiver = fa.receiver.resolve(markers, context);
				}
				IValue v1 = fa.resolveFieldAccess(markers, context);
				if (v1 != null)
				{
					this.receiver = v1;
					this.arguments.resolve(markers, context);
				}
				else
				{
					this.arguments.resolve(markers, context);
					ICodePosition position = this.arguments.getFirstValue().getPosition().to(this.arguments.getLastValue().getPosition());
					ArrayExpr array = new ArrayExpr(position, ((ArgumentList) this.arguments).getValues(), this.arguments.size());
					IArguments arguments = new SingleArgument(array);
					
					IMethod m = ICall.resolveMethod(context, fa.receiver, fa.name, arguments);
					if (m != null)
					{
						MethodCall mc = new MethodCall(fa.position, fa.receiver, fa.name);
						mc.method = m;
						mc.arguments = arguments;
						mc.dotless = fa.dotless;
						mc.checkArguments(markers, context);
						return mc;
					}
					
					ICall.addResolveMarker(markers, this.position, fa.receiver, fa.name, arguments);
					return this;
				}
			}
			else
			{
				this.receiver = this.receiver.resolve(markers, context);
				this.arguments.resolve(markers, context);
			}
		}
		else
		{
			this.arguments.resolve(markers, context);
		}
		
		IMethod m = ICall.resolveMethod(context, this.receiver, Names.subscript, this.arguments);
		if (m != null)
		{
			this.method = m;
			this.checkArguments(markers, context);
			return this;
		}
		
		ICall.addResolveMarker(markers, this.position, this.receiver, Names.subscript, this.arguments);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		
		buffer.append('[');
		int count = this.arguments.size();
		this.arguments.getValue(0, null).toString(prefix, buffer);
		for (int i = 1; i < count; i++)
		{
			buffer.append(Formatting.Expression.arraySeperator);
			this.arguments.getValue(i, null).toString(prefix, buffer);
		}
		buffer.append(']');
	}
}
