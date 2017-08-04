package dyvil.tools.compiler.ast.member;

import dyvil.lang.Name;

public interface INamed
{
	void setName(Name name);
	
	Name getName();
}
