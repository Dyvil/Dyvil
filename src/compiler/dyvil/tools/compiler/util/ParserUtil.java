package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class ParserUtil
{
	public static boolean isWhitespace(char c)
	{
		return c <= ' ';
	}
	
	public static boolean isOpenBracket(char c)
	{
		return c == '(' || c == '[' || c == '{';
	}
	
	public static boolean isCloseBracket(char c)
	{
		return c == ')' || c == ']' || c == '}';
	}
	
	public static boolean isBinDigit(char c)
	{
		return c == '0' || c == '1';
	}
	
	public static boolean isOctDigit(char c)
	{
		return c >= '0' && c <= '7';
	}
	
	public static boolean isDigit(char c)
	{
		return c >= '0' && c <= '9';
	}
	
	public static boolean isHexDigit(char c)
	{
		return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
	}
	
	public static boolean isLetter(char c)
	{
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}
	
	public static boolean isSymbol(char c)
	{
		return c == '.' || c == ',' || c == ';';
	}
	
	public static boolean isIdentifierPart(char c)
	{
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}
	
	public static boolean isIdentifierSymbol(char c)
	{
		return c >= '!' && c <= '~' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && !isSymbol(c) && !isOpenBracket(c)
				&& !isCloseBracket(c);
	}
	
	public static boolean isSeperator(char c)
	{
		return c == ',' || c == ';';
	}
	
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
		return type == Symbols.COMMA || type == Symbols.SEMICOLON || (type & Symbols.CLOSE_BRACKET) == Symbols.CLOSE_BRACKET;
	}
	
	public static boolean isTerminator2(int type)
	{
		return type == Symbols.DOT || type == Symbols.COMMA || type == Symbols.SEMICOLON || type == Symbols.EQUALS || type == Keywords.IS
				|| type == Keywords.AS || (type & Symbols.CLOSE_BRACKET) == Symbols.CLOSE_BRACKET || type == Symbols.OPEN_SQUARE_BRACKET
				|| type == Tokens.STRING_PART || type == Tokens.STRING_END;
	}
	
	public static boolean isSeperator(int type)
	{
		return type == Symbols.COMMA || type == Symbols.SEMICOLON;
	}
	
	public static boolean isOperator(IParserManager pm, IToken token, int type)
	{
		if (type == Tokens.SYMBOL_IDENTIFIER || type == Tokens.DOT_IDENTIFIER)
		{
			return true;
		}
		return pm.getOperator(token.nameValue()) != null;
	}
	
	public static IValue parsePrimitive(IToken token, int type) throws SyntaxError
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
