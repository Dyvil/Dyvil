package dyvil.tools.compiler.ast.access;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface ICall extends IValue
{
	public void setArguments(IArguments arguments);
	
	public IArguments getArguments();
	
	public static void addResolveMarker(MarkerList markers, ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		if (arguments == EmptyArguments.INSTANCE)
		{
			Marker marker = markers.create(position, "resolve.method_field", name);
			if (instance != null)
			{
				marker.addInfo("Callee Type: " + instance.getType());
			}
			return;
		}
		
		Marker marker = markers.create(position, "resolve.method", name);
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
		List<MethodMatch> matches = new ArrayList();
		if (instance != null)
		{
			IType type = instance.getType();
			if (type != null)
			{
				type.getMethodMatches(matches, instance, name, arguments);
				
				if (!matches.isEmpty())
				{
					return IContext.getBestMethod(matches);
				}
			}
		}
		
		context.getMethodMatches(matches, instance, name, arguments);
		if (!matches.isEmpty())
		{
			return IContext.getBestMethod(matches);
		}
		
		Types.PREDEF_CLASS.getMethodMatches(matches, instance, name, arguments);
		if (!matches.isEmpty())
		{
			return IContext.getBestMethod(matches);
		}
		
		if (instance == null && arguments.size() == 1)
		{
			IValue v = arguments.getFirstValue();
			IType type = v.getType();
			if (type != null)
			{
				type.getMethodMatches(matches, instance, name, EmptyArguments.INSTANCE);
				
				if (!matches.isEmpty())
				{
					return IContext.getBestMethod(matches);
				}
			}
		}
		
		return null;
	}
}
