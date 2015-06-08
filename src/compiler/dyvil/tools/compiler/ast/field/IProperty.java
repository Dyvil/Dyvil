package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;

public interface IProperty extends IField
{
	public void setAccess(byte access);
	
	public byte getAccess();
	
	public void setGetter(IValue get);
	
	public IValue getGetter();
	
	public void setSetter(IValue set);
	
	public IValue getSetter();
}
