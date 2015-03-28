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
	protected IValueList	valueList;
	
	private String			label;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
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
		
		if (this.mode == 0)
		{
			if (ParserUtil.isIdentifier(type) && token.next().type() == Tokens.COLON)
			{
				this.label = token.nameValue().qualified;
				pm.skip();
				return;
			}
			
			this.mode = 1;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		}
		if (this.mode == 1)
		{
			if (type == Tokens.COMMA)
			{
				this.valueList.setArray(true);
				this.mode = 0;
				return;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.mode = 0;
				return;
			}
			if (token.prev().type() == Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.pushParser(new ExpressionParser(this), true);
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
			this.valueList.addValue(value, new Label(Name.get(this.label), value));
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
