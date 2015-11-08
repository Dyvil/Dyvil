package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface ICall extends IValue
{
	public void setArguments(IArguments arguments);
	
	public IArguments getArguments();
	
	public static void addResolveMarker(MarkerList markers, ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		if (arguments == EmptyArguments.INSTANCE)
		{
			Marker marker = I18n.createMarker(position, "resolve.method_field", name);
			if (instance != null)
			{
				marker.addInfo("Callee Type: " + instance.getType());
			}
			
			markers.add(marker);
			return;
		}
		
		Marker marker = I18n.createMarker(position, "resolve.method", name);
		if (instance != null)
		{
			marker.addInfo("Callee Type: " + instance.getType());
		}
		if (!arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}
		
		markers.add(marker);
	}
	
	public static boolean privateAccess(IContext context, IValue instance)
	{
		return instance == null || context.getThisClass() == instance.getType().getTheClass();
	}
	
	public static IDataMember resolveField(IContext context, ITyped instance, Name name)
	{
		IDataMember match;
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
	
	public static IMethod resolveMethod(IContext context, IValue instance, Name name, IArguments arguments)
	{
		MethodMatchList matches = new MethodMatchList();
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				type.getMethodMatches(matches, instance, name, arguments);
				
				if (!matches.isEmpty())
				{
					return matches.getBestMethod();
				}
			}
		}
		
		context.getMethodMatches(matches, instance, name, arguments);
		if (!matches.isEmpty())
		{
			return matches.getBestMethod();
		}
		
		// Prefix Methods
		if (arguments.size() == 1)
		{
			IValue v = arguments.getFirstValue();
			IType type = v.getType();
			if (type != null)
			{
				type.getMethodMatches(matches, instance, name, arguments);
				
				if (!matches.isEmpty())
				{
					return matches.getBestMethod();
				}
			}
		}
		
		Types.LANG_HEADER.getMethodMatches(matches, instance, name, arguments);
		if (!matches.isEmpty())
		{
			return matches.getBestMethod();
		}
		
		return null;
	}
}
