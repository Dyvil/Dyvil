package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class ExpressionListParser extends Parser implements IValued
{
	private static final int	EXPRESSION	= 1;
	private static final int	SEPARATOR	= 2;
	
	protected IValueList		valueList;
	
	private Name				label;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
		this.mode = EXPRESSION;
	}
	
	@Override
	public void reset()
	{
		this.mode = EXPRESSION;
		this.label = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == EXPRESSION)
		{
			if (ParserUtil.isIdentifier(type) && token.next().type() == Tokens.COLON)
			{
				this.label = token.nameValue();
				pm.skip();
				return;
			}
			
			this.mode = SEPARATOR;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		}
		if (this.mode == SEPARATOR)
		{
			if (type == Tokens.COMMA)
			{
				this.valueList.setArray(true);
				this.mode = EXPRESSION;
				return;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.mode = EXPRESSION;
				return;
			}
			if (token.prev().type() == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.mode = EXPRESSION;
				pm.reparse();
				return;
			}
			throw new SyntaxError(token, "Invalid Expression List - ',' or ';' expected");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.label != null)
		{
			this.valueList.addValue(value, new Label(this.label, value));
			this.label = null;
		}
		else
		{
			this.valueList.addValue(value);
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
