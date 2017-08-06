package dyvilx.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.parsing.lexer.LexerUtil;

public class StringValue implements Expression
{
	private SourcePosition position;
	private final String value;

	public StringValue(SourcePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public String evaluateString(Scope scope)
	{
		return this.value;
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		LexerUtil.appendStringLiteral(this.value, builder);
	}
}
