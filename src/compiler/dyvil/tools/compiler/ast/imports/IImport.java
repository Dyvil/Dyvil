package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IImport extends IASTNode, IContext
{
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic);
	
	@Override
	public default boolean isStatic()
	{
		return false;
	}
	
	@Override
	public default IType getThisType()
	{
		return null;
	}
	
	@Override
	public default ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public default ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public default void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public default byte getAccessibility(IMember member)
	{
		return READ_WRITE_ACCESS;
	}
}
