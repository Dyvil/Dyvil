package dyvil.tools.gensrc.ast.expression;

import dyvil.tools.gensrc.ast.scope.Scope;

public interface Expression
{
	<T> T evaluate(Class<T> type, Scope replacements);
}
