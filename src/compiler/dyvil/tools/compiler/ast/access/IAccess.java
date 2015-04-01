package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IAccess extends IValue, IValued, ICall
{
	public boolean isResolved();
	
	public boolean resolve(IContext context, MarkerList markers);
	
	public IValue resolve2(IContext context);
	
	public IAccess resolve3(IContext context, IAccess next);
	
	public void addResolveError(MarkerList markers);
	
	public static IField resolveField(IContext context, ITyped instance, Name name)
	{
		FieldMatch match;
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				match = type.resolveField(name);
				if (match != null)
				{
					return match.theField;
				}
			}
		}
		
		match = context.resolveField(name);
		if (match != null)
		{
			return match.theField;
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
