package dyvil.tools.compiler.ast.context;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;

public interface IStaticContext extends IContext
{
	@Override
	public default boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IDyvilHeader getHeader();
	
	@Override
	public default IClass getThisClass()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name);
	
	@Override
	public IClass resolveClass(Name name);
	
	@Override
	public IType resolveType(Name name);
	
	@Override
	public default ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name);
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	@Override
	public default void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		
	}
	
	@Override
	public default byte getVisibility(IClassMember member)
	{
		return 0;
	}
	
	@Override
	public default boolean handleException(IType type)
	{
		return false;
	}
}
