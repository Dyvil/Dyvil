package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IAccess extends INamed, IValue, IValued, IValueList
{
	public boolean resolve(IContext context);
	
	public IAccess resolve2(IContext context);
	
	public IAccess resolve3(IContext context, IAccess next);
	
	public Marker getResolveError();
	
	public static IField resolveField(IContext context, ITyped instance, String name)
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
	
	public static IMethod resolveMethod(IContext context, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		MethodMatch match;
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				match = type.resolveMethod(null, name, arguments);
				if (match != null)
				{
					return match.theMethod;
				}
			}
		}
		
		match = context.resolveMethod(instance, name, arguments);
		if (match != null)
		{
			return match.theMethod;
		}
		return null;
	}
}
