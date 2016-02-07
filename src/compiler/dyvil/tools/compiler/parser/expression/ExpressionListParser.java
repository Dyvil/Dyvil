package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class ExpressionListParser extends Parser implements IValueConsumer
{
	private static final int EXPRESSION = 0;
	private static final int COMMA = 1;

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
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case EXPRESSION:
			this.mode = COMMA;
			pm.pushParser(pm.newExpressionParser(this), true);
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
		this.valueList.addValue(value);
	}
}
