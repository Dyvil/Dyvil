package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;

public interface IDefaultContext extends IStaticContext
{
	@Override
	public default IDyvilHeader getHeader()
	{
		return null;
	}
	
	@Override
	public default Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public default IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public default IType resolveType(Name name)
	{
		return null;
	}
	
	@Override
	public default IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public default void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
	}
}
