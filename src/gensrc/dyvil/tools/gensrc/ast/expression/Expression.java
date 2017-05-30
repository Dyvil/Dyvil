package dyvil.tools.gensrc.ast.expression;

import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.ASTNode;

public interface Expression extends ASTNode
{
	default boolean evaluateBoolean(Scope scope)
	{
		return Boolean.parseBoolean(this.evaluateString(scope));
	}

	default long evaluateInteger(Scope scope)
	{
		try
		{
			return Long.parseLong(this.evaluateString(scope));
		}
		catch (NumberFormatException ex)
		{
			return 0L;
		}
	}

	default double evaluateDouble(Scope scope)
	{
		try
		{
			return Double.parseDouble(this.evaluateString(scope));
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	String evaluateString(Scope scope);

	default ExpressionList evaluateList(Scope scope)
	{
		return new ExpressionList(this);
	}

	@Override
	void toString(String indent, StringBuilder builder);
}
