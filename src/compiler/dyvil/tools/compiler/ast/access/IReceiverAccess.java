package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.expression.IValue;

public interface IReceiverAccess
{
	public IValue getReceiver();
	
	public void setReceiver(IValue receiver);
}
