package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.api.IValueMap;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ExpressionMapParser extends Parser implements IValued
{
	public static final int	NAME		= 1;
	public static final int	VALUE		= 2;
	public static final int	SEPERATOR	= 4;
	
	protected IContext		context;
	protected IValueMap		valueMap;
	
	private Object			key;
	
	public ExpressionMapParser(IContext context, IValueMap valueMap)
	{
		this.context = context;
		this.valueMap = valueMap;
		this.mode = NAME | VALUE;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if ("}".equals(value) || ")".equals(value))
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.isInMode(NAME))
		{
			if (token.next().equals("="))
			{
				this.key = value;
				pm.skip();
				return true;
			}
			this.mode = VALUE;
		}
		if (this.isInMode(VALUE))
		{
			this.mode = SEPERATOR;
			pm.pushTryParser(new ExpressionParser(this.context, this), token, true);
			return true;
		}
		if (this.isInMode(SEPERATOR))
		{
			if (",".equals(value))
			{
				this.mode = NAME | VALUE;
				return true;
			}
		}
		
		pm.popParser(true);
		return true;
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
