package dyvil.tools.compiler.ast.member;

import dyvil.tools.parsing.Name;

public interface INamed
{
	public void setName(Name name);
	
	public Name getName();
}
