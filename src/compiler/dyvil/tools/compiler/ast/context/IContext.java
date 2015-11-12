package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.parsing.Name;

public interface IContext
{
	public static final byte	VISIBLE		= 0;
	public static final byte	INVISIBLE	= 1;
	public static final byte	INTERNAL	= 2;
	
	public boolean isStatic();
	
	public IDyvilHeader getHeader();
	
	public IClass getThisClass();
	
	public Package resolvePackage(Name name);
	
	public IClass resolveClass(Name name);
	
	public IType resolveType(Name name);
	
	public ITypeVariable resolveTypeVariable(Name name);
	
	public IDataMember resolveField(Name name);
	
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);
	
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments);
	
	public boolean handleException(IType type);
	
	public boolean isMember(IVariable variable);
	
	public IDataMember capture(IVariable capture);
	
	public IAccessible getAccessibleThis(IClass type);
	
	public IAccessible getAccessibleImplicit();
	
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
		ConstructorMatchList matches = new ConstructorMatchList();
		context.getConstructorMatches(matches, arguments);
		return matches.getBestConstructor();
	}
	
	public static IMethod resolveMethod(IContext context, IValue instance, Name name, IArguments arguments)
	{
		MethodMatchList matches = new MethodMatchList();
		context.getMethodMatches(matches, instance, name, arguments);
		return matches.getBestMethod();
	}
	
	public static byte getVisibility(IContext context, IClassMember member)
	{
		IClass thisClass = context.getThisClass();
		if (thisClass != null)
		{
			return thisClass.getVisibility(member);
		}
		
		return context.getHeader().getVisibility(member);
	}
}
