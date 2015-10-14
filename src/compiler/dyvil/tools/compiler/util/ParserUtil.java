package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.parsing.token.IToken;

public class ParserUtil
{
	// Token Type Utilities
	
	public static boolean isIdentifier(int type)
	{
		return (type & Tokens.IDENTIFIER) != 0;
	}
	
	public static boolean isCloseBracket(int type)
	{
		return (type & Symbols.CLOSE_BRACKET) == Symbols.CLOSE_BRACKET;
	}
	
	public static boolean isTerminator(int type)
	{
		switch (type)
		{
		case Symbols.COMMA:
		case Symbols.SEMICOLON:
		case Symbols.COLON:
		case Symbols.CLOSE_CURLY_BRACKET:
		case Symbols.CLOSE_PARENTHESIS:
		case Symbols.CLOSE_SQUARE_BRACKET:
			return true;
		}
		return false;
	}
	
	public static boolean isExpressionTerminator(int type)
	{
		if (isTerminator(type))
		{
			return true;
		}
		switch (type)
		{
		case Symbols.DOT:
		case Symbols.EQUALS:
		case Keywords.IS:
		case Keywords.AS:
		case Keywords.MATCH:
		case Symbols.OPEN_SQUARE_BRACKET:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			return true;
		}
		return false;
	}
	
	public static boolean isSeperator(int type)
	{
		return type == Symbols.COMMA || type == Symbols.SEMICOLON;
	}
	
	public static boolean isOperator(IParserManager pm, IToken token, int type)
	{
		if (type == Tokens.SYMBOL_IDENTIFIER)
		{
			return true;
		}
		return pm.getOperator(token.nameValue()) != null;
	}
	
	public static IValue parsePrimitive(IToken token, int type)
	{
		switch (type)
		{
		case Keywords.TRUE:
			return new BooleanValue(token.raw(), true);
		case Keywords.FALSE:
			return new BooleanValue(token.raw(), false);
		case Tokens.STRING:
			return new StringValue(token.raw(), token.stringValue());
		case Tokens.CHAR:
			return new CharValue(token.raw(), token.charValue());
		case Tokens.INT:
			return new IntValue(token.raw(), token.intValue());
		case Tokens.LONG:
			return new LongValue(token.raw(), token.longValue());
		case Tokens.FLOAT:
			return new FloatValue(token.raw(), token.floatValue());
		case Tokens.DOUBLE:
			return new DoubleValue(token.raw(), token.doubleValue());
		}
		return null;
	}
}
