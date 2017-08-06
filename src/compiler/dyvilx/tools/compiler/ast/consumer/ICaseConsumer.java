package dyvilx.tools.compiler.ast.consumer;

import dyvilx.tools.compiler.ast.expression.MatchCase;

public interface ICaseConsumer
{
	void addCase(MatchCase matchCase);
}
