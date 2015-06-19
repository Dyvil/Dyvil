package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SubscriptGetter extends AbstractCall
{
	private Array	argument;
	
	public SubscriptGetter(ICodePosition position)
	{
		this.position = position;
		
		this.argument = new Array();
		this.arguments = new SingleArgument(this.argument);
	}
	
	public SubscriptGetter(ICodePosition position, IValue instance)
	{
		this.position = position;
		this.instance = instance;
		
		this.argument = new Array();
		this.arguments = new SingleArgument(this.argument);
	}
	
	public Array getArray()
	{
		return this.argument;
	}
	
	@Override
	public int valueTag()
	{
		return SUBSCRIPT_GET;
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
				IValue v1 = fa.resolveFieldAccess(context);
				if (v1 != null)
				{
					this.instance = v1;
					this.argument.resolve(markers, context);
				}
				else
				{
					this.argument.resolve(markers, context);
					IMethod m = ICall.resolveMethod(context, fa.instance, fa.name, this.arguments);
					if (m != null)
					{
						MethodCall mc = new MethodCall(fa.position, fa.instance, fa.name);
						mc.method = m;
						mc.arguments = this.arguments;
						mc.dotless = fa.dotless;
						return mc;
					}
					
					ICall.addResolveMarker(markers, this.position, fa.instance, fa.name, this.arguments);
					return this;
				}
			}
			else
			{
				this.instance = this.instance.resolve(markers, context);
				this.argument.resolve(markers, context);
			}
		}
		else
		{
			this.argument.resolve(markers, context);
		}
		
		int count = this.argument.valueCount();
		ArgumentList argumentList = new ArgumentList(count);
		for (int i = 0; i < count; i++)
		{
			argumentList.addValue(this.argument.getValue(i));
		}
		
		IMethod m = ICall.resolveMethod(context, this.instance, Name.subscript, argumentList);
		if (m != null)
		{
			this.arguments = argumentList;
			this.method = m;
			return this;
		}
		
		ICall.addResolveMarker(markers, position, this.instance, Name.subscript, argumentList);
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
		int count = this.argument.valueCount();
		this.argument.getValue(0).toString(prefix, buffer);
		for (int i = 1; i < count; i++)
		{
			buffer.append(Formatting.Expression.arraySeperator);
			this.argument.getValue(i).toString(prefix, buffer);
		}
		buffer.append(']');
	}
}
