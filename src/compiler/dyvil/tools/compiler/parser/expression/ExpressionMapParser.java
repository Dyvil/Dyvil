package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ExpressionMapParser extends Parser implements IValued
{
	public static final int	NAME		= 1;
	public static final int	VALUE		= 2;
	public static final int	SEPERATOR	= 4;
	
	protected IValueMap		valueMap;
	
	private Object			key;
	
	public ExpressionMapParser(IValueMap valueMap)
	{
		this.valueMap = valueMap;
		this.mode = NAME;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.mode == NAME)
		{
			if (token.next().type() == Tokens.EQUALS)
			{
				this.key = token.value();
				pm.skip();
				return true;
			}
			this.mode = VALUE;
		}
		if (this.mode == VALUE)
		{
			this.mode = SEPERATOR;
			pm.pushTryParser(new ExpressionParser(this), token, true);
			return true;
		}
		if (this.mode == SEPERATOR)
		{
			if (type == Tokens.COMMA)
			{
				this.mode = NAME | VALUE;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueMap.addValue(this.key, value);
		this.key = null;
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
