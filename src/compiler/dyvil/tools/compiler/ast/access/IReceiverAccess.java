package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.parsing.marker.MarkerList;

public interface IReceiverAccess
{
	public IValue getReceiver();
	
	public void setReceiver(IValue receiver);
	
	public void resolveReceiver(MarkerList markers, IContext context);
}
