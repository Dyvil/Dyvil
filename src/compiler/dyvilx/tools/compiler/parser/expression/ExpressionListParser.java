package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.IValueList;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public final class ExpressionListParser extends Parser implements IValueConsumer
{
	private static final int EXPRESSION = 0;
	private static final int COMMA      = 1;

	protected IValueList valueList;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
		this.mode = EXPRESSION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (BaseSymbols.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case EXPRESSION:
			this.mode = COMMA;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		case COMMA:
			if (type == BaseSymbols.COMMA || type == BaseSymbols.SEMICOLON && token.isInferred())
			{
				this.mode = EXPRESSION;
				return;
			}
			pm.report(token, "expression.list.comma");
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueList.add(value);
	}
}
