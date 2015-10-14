package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.token.IToken;

public final class ExpressionListParser extends Parser implements IValueConsumer
{
	protected IValueList valueList;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
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
		case 0:
			this.mode = 1;
			pm.pushParser(pm.newExpressionParser(this), true);
			return;
		case 1:
			if (type == Symbols.COMMA || type == Symbols.SEMICOLON)
			{
				this.mode = 0;
				return;
			}
			pm.report(token, "Invalid Expression List - ',' expected");
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueList.addValue(value);
	}
}
