package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ExpressionListParser extends Parser implements IValued
{
	protected IValueList	valueList;
	private String			label;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
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
		
		if (this.mode == 0)
		{
			if (token.next().isType(Tokens.COLON))
			{
				this.label = token.value();
				pm.skip();
				return true;
			}
			
			this.mode = 1;
			pm.pushParser(new ExpressionParser(this), true);
			return true;
		}
		if (this.mode == 1)
		{
			if (type == Tokens.COMMA)
			{
				this.valueList.setArray(true);
				this.mode = 0;
				return true;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.mode = 0;
				return true;
			}
			if (token.prev().isType(Tokens.CLOSE_CURLY_BRACKET))
			{
				pm.pushParser(new ExpressionParser(this), true);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueList.addValue(value);
		
		if (this.label != null)
		{
			this.valueList.addLabel(this.label, value);
			this.label = null;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
