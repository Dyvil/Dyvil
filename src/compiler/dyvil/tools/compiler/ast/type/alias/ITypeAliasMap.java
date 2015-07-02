package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.compiler.ast.member.Name;

public interface ITypeAliasMap
{
	public void addTypeAlias(ITypeAlias typeAlias);
	
	public ITypeAlias getTypeAlias(Name name);
}
