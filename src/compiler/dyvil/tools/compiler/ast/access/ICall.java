package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.parameter.IArguments;

public interface ICall
{
	public void setArguments(IArguments arguments);
	
	public IArguments getArguments();
}
