package dyvilx.tools.gensrc.ast.expression;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.parsing.ASTNode;

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

	default Iterable<Expression> evaluateIterable(Scope scope)
	{
		final SourcePosition position = this.getPosition();
		final String string = this.evaluateString(scope);
		if (string == null || string.isEmpty())
		{
			return EmptyIterator::apply;
		}

		if (string.indexOf(',') < 0)
		{
			final StringValue value = new StringValue(position, string);
			return () -> new SingletonIterator<>(value);
		}

		final String[] split = string.split("\\s*,\\s*");
		final Expression[] array = new Expression[split.length];
		for (int i = 0; i < split.length; i++)
		{
			array[i] = new StringValue(position, split[i]);
		}
		return () -> new ArrayIterator<>(array);
	}

	@Override
	void toString(String indent, StringBuilder builder);
}
