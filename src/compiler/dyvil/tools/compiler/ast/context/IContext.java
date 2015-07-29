package dyvil.tools.compiler.ast.context;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.IClassCompilable;

public interface IContext
{
	public static final byte	VISIBLE		= 0;
	public static final byte	INVISIBLE	= 1;
	public static final byte	SEALED		= 2;
	
	public boolean isStatic();
	
	public IDyvilHeader getHeader();
	
	public IClass getThisClass();
	
	public Package resolvePackage(Name name);
	
	public IClass resolveClass(Name name);
	
	public IType resolveType(Name name);
	
	public ITypeVariable resolveTypeVariable(Name name);
	
	public IDataMember resolveField(Name name);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	public boolean handleException(IType type);
	
	public IVariable capture(IVariable variable);
	
	public IAccessible getAccessibleThis(IClass type);
	
	public static void addCompilable(IContext context, IClassCompilable compilable)
	{
		IClass iclass = context.getThisClass();
		if (iclass != null)
		{
			iclass.addCompilable(compilable);
			return;
		}
		
		IDyvilHeader header = context.getHeader();
		header.addInnerClass(compilable);
	}
	
	public static IClass resolveClass(IContext context, Name name)
	{
		IClass iclass = context.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		
		return Types.LANG_HEADER.resolveClass(name);
	}
	
	public static IType resolveType(IContext context, Name name)
	{
		IType itype = context.resolveType(name);
		if (itype != null)
		{
			return itype;
		}
		
		IClass iclass = Types.LANG_HEADER.resolveClass(name);
		if (iclass != null)
		{
			return new ClassType(iclass);
		}
		
		return null;
	}
	
	public static IConstructor resolveConstructor(IContext context, IArguments arguments)
	{
		List<ConstructorMatch> matches = new ArrayList();
		context.getConstructorMatches(matches, arguments);
		return getBestConstructor(matches);
	}
	
	public static IMethod resolveMethod(IContext context, IValue instance, Name name, IArguments arguments)
	{
		List<MethodMatch> matches = new ArrayList();
		context.getMethodMatches(matches, instance, name, arguments);
		return getBestMethod(matches);
	}
	
	public static IMethod getBestMethod(List<MethodMatch> matches)
	{
		int size = matches.size();
		switch (size)
		{
		case 0:
			return null;
		case 1:
			return matches.get(0).method;
		default:
			MethodMatch bestMethod = matches.get(0);
			float bestMatch = bestMethod.match;
			for (int i = 1; i < size; i++)
			{
				MethodMatch m = matches.get(i);
				if (m.match < bestMatch)
				{
					bestMethod = m;
					bestMatch = m.match;
				}
			}
			
			return bestMethod.method;
		}
	}
	
	public static IConstructor getBestConstructor(List<ConstructorMatch> matches)
	{
		int size = matches.size();
		switch (size)
		{
		case 0:
			return null;
		case 1:
			return matches.get(0).constructor;
		default:
			ConstructorMatch bestConstructor = matches.get(0);
			float bestMatch = bestConstructor.match;
			for (int i = 1; i < size; i++)
			{
				ConstructorMatch m = matches.get(i);
				if (m.match < bestMatch)
				{
					bestConstructor = m;
					bestMatch = m.match;
				}
			}
			return bestConstructor.constructor;
		}
	}
}
