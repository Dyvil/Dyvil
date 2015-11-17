package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.type.IType;

public interface IExceptionList
{
	int exceptionCount();
	
	void setException(int index, IType exception);
	
	void addException(IType exception);
	
	IType getException(int index);
}
