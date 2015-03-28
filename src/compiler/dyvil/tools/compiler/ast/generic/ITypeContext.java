package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	public IType resolveType(Name name);
}
