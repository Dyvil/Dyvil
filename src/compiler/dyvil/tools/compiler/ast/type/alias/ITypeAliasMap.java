package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.parsing.Name;

public interface ITypeAliasMap
{
	void addTypeAlias(ITypeAlias typeAlias);
	
	ITypeAlias resolveTypeAlias(Name name, int arity);
}
