package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.value.IValue;

public interface IProperty extends IField
{
	public void setGetter(IValue get);
	
	public IValue getGetter();
	
	public void setSetter(IValue set);
	
	public IValue getSetter();
}
