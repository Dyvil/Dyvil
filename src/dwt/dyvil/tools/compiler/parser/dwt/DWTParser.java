package dyvil.tools.compiler.parser.dwt;

import dyvil.tools.compiler.ast.dwt.DWTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class DWTParser extends Parser implements IValued
{
	public static final int	NAME			= 1;
	public static final int	BODY			= 2;
	public static final int	PROPERTY_NAME	= 4;
	public static final int	EQUALS			= 8;
	public static final int	BODY_END		= 64;
	
	protected DWTNode node;
	
	private String name;
	
	public DWTParser(DWTNode node)
	{
		this.node = node;
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.mode = NAME;
		this.name = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			this.mode = BODY;
			if (ParserUtil.isIdentifier(type))
			{
				this.node.setPosition(token.raw());
				this.node.name = token.nameValue();
				return;
			}
			throw new SyntaxError(token, "Invalid DWT File - Name expected");
		}
		if (this.mode == BODY)
		{
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.mode = PROPERTY_NAME;
				return;
			}
			throw new SyntaxError(token, "Invalid Body - '{' expected");
		}
		if (type == Symbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser();
			return;
		}
		if (this.mode == PROPERTY_NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.mode = EQUALS;
				this.name = token.nameValue().qualified;
				return;
			}
			this.mode = PROPERTY_NAME;
			throw new SyntaxError(token, "Invalid Property - Name expected");
		}
		if (this.mode == EQUALS)
		{
			if (type == Symbols.EQUALS || type == Symbols.COLON)
			{
				pm.pushParser(new DWTValueParser(this));
				return;
			}
			this.mode = PROPERTY_NAME;
			throw new SyntaxError(token, "Invalid Property - '=' expected");
		}
		if (this.mode == BODY_END)
		{
			// type == Tokens.CLOSE_CURLY_BRACKET was already handled above, so
			// throw a SyntaxError
			throw new SyntaxError(token, "Invalid Body - '}' expected");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.mode = PROPERTY_NAME;
		this.node.addValue(Name.get(this.name), value);
		this.name = null;
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
