package dyvil.tools.gensrc.ast.expression;

import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.ASTNode;

public interface Expression extends ASTNode
{
	boolean evaluateBoolean(Scope scope);

	long evaluateInteger(Scope scope);

	double evaluateDouble(Scope scope);

	String evaluateString(Scope scope);

	default ExpressionList evaluateList(Scope scope)
	{
		return new ExpressionList(this);
	}

	@Override
	void toString(String indent, StringBuilder builder);
}
