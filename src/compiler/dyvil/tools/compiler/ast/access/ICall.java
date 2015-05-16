package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface ICall extends IValue
{
	public void setArguments(IArguments arguments);
	
	public IArguments getArguments();
	
	public static IField resolveField(IContext context, ITyped instance, Name name)
	{
		IField match;
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				match = type.resolveField(name);
				if (match != null)
				{
					return match;
				}
			}
			
			return null;
		}
		
		match = context.resolveField(name);
		if (match != null)
		{
			return match;
		}
		return null;
	}
	
	public static IMethod resolveMethod(MarkerList markers, IContext context, IValue instance, Name name, IArguments arguments)
	{
		IMethod match;
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				match = IContext.resolveMethod(markers, type, instance, name, arguments);
				if (match != null)
				{
					return match;
				}
			}
		}
		else if (arguments.size() == 1)
		{
			IValue v = arguments.getFirstValue();
			IType type = v.getType();
			if (type != null)
			{
				match = IContext.resolveMethod(markers, type, instance, name, EmptyArguments.INSTANCE);
				if (match != null)
				{
					return match;
				}
			}
		}
		
		match = IContext.resolveMethod(markers, context, instance, name, arguments);
		if (match != null)
		{
			return match;
		}
		
		match = IContext.resolveMethod(markers, Types.PREDEF_CLASS, instance, name, arguments);
		if (match != null)
		{
			return match;
		}
		return null;
	}
}
