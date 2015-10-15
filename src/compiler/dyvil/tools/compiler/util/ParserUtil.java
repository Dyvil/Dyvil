package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
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
		return (type & BaseSymbols.CLOSE_BRACKET) == BaseSymbols.CLOSE_BRACKET;
	}
	
	public static boolean isTerminator(int type)
	{
		switch (type)
		{
		case BaseSymbols.COMMA:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.COLON:
		case BaseSymbols.CLOSE_CURLY_BRACKET:
		case BaseSymbols.CLOSE_PARENTHESIS:
		case BaseSymbols.CLOSE_SQUARE_BRACKET:
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
		case BaseSymbols.DOT:
		case BaseSymbols.EQUALS:
		case DyvilKeywords.IS:
		case DyvilKeywords.AS:
		case DyvilKeywords.MATCH:
		case BaseSymbols.OPEN_SQUARE_BRACKET:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			return true;
		}
		return false;
	}
	
	public static boolean isSeperator(int type)
	{
		return type == BaseSymbols.COMMA || type == BaseSymbols.SEMICOLON;
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
		case DyvilKeywords.TRUE:
			return new BooleanValue(token.raw(), true);
		case DyvilKeywords.FALSE:
			return new BooleanValue(token.raw(), false);
		case Tokens.STRING:
			return new StringValue(token.raw(), token.stringValue());
		case Tokens.CHAR:
			return new CharValue(token.raw(), token.stringValue());
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
