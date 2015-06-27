package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.classes.IClass;

public interface IClassMember extends IMember, IClassCompilable
{
	public IClass getTheClass();
	
	public void setTheClass(IClass iclass);
}
