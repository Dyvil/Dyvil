package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.config.Formatting;
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
		this.instance = instance;
		this.arguments = new ArgumentList();
	}
	
	public SubscriptGetter(ICodePosition position, IValue instance, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
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
	protected IValue resolveCall(MarkerList markers, IContext context)
	{
		return null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			if (this.instance.valueTag() == FIELD_ACCESS)
			{
				FieldAccess fa = (FieldAccess) this.instance;
				if (fa.instance != null)
				{
					fa.instance = fa.instance.resolve(markers, context);
				}
				IValue v1 = fa.resolveFieldAccess(markers, context);
				if (v1 != null)
				{
					this.instance = v1;
					this.arguments.resolve(markers, context);
				}
				else
				{
					this.arguments.resolve(markers, context);
					ICodePosition position = this.arguments.getFirstValue().getPosition().to(this.arguments.getLastValue().getPosition());
					Array array = new Array(position, ((ArgumentList) this.arguments).getValues(), this.arguments.size());
					IArguments arguments = new SingleArgument(array);
					
					IMethod m = ICall.resolveMethod(context, fa.instance, fa.name, arguments);
					if (m != null)
					{
						MethodCall mc = new MethodCall(fa.position, fa.instance, fa.name);
						mc.method = m;
						mc.arguments = arguments;
						mc.dotless = fa.dotless;
						mc.checkArguments(markers, context);
						return mc;
					}
					
					ICall.addResolveMarker(markers, this.position, fa.instance, fa.name, arguments);
					return this;
				}
			}
			else
			{
				this.instance = this.instance.resolve(markers, context);
				this.arguments.resolve(markers, context);
			}
		}
		else
		{
			this.arguments.resolve(markers, context);
		}
		
		int count = this.arguments.size();
		ArgumentList argumentList = new ArgumentList(count);
		for (int i = 0; i < count; i++)
		{
			argumentList.addValue(this.arguments.getValue(i, null));
		}
		
		IMethod m = ICall.resolveMethod(context, this.instance, Name.subscript, argumentList);
		if (m != null)
		{
			this.arguments = argumentList;
			this.method = m;
			this.checkArguments(markers, context);
			return this;
		}
		
		ICall.addResolveMarker(markers, this.position, this.instance, Name.subscript, argumentList);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
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
