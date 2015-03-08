package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.type.IType;

public interface IExceptionList
{
	public void exceptionCount();
	
	public void setException(int index, IType exception);
	
	public void addException(IType exception);
	
	public IType getException(int index);
}
