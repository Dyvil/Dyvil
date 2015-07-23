package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.expression.FormatStringExpression;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Tokens;

public final class FormatStringParser extends Parser implements IValued
{
	protected FormatStringExpression value;
	
	public FormatStringParser(FormatStringExpression value)
	{
		this.value = value;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		switch (type)
		{
		case Tokens.STRING_START:
		case Tokens.STRING_PART:
		{
			int nextType = token.next().type();
			if (nextType == Tokens.STRING_PART || nextType == Tokens.STRING_END)
			{
				throw new SyntaxError(token.next(), "Invalid Format String - Expression expected");
			}
			this.value.addString(token.stringValue());
			pm.pushParser(pm.newExpressionParser(this));
			return;
		}
		case Tokens.STRING_END:
			this.value.addString(token.stringValue());
			pm.popParser();
			return;
		}
		throw new SyntaxError(token, "Invalid Format String - String part expected");
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value.addValue(value);
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
