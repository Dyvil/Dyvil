package dyvil.tools.compiler.ast.member;

import dyvil.tools.parsing.Name;

public interface INamed
{
	void setName(Name name);
	
	Name getName();
}
