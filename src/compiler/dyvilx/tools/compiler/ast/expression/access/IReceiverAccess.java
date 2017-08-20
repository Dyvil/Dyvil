package dyvilx.tools.compiler.ast.expression.access;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IReceiverAccess
{
	IValue getReceiver();
	
	void setReceiver(IValue receiver);
	
	void resolveReceiver(MarkerList markers, IContext context);
}
