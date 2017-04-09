package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.expression.MatchCase;

public interface ICaseConsumer
{
	void addCase(MatchCase matchCase);
}
