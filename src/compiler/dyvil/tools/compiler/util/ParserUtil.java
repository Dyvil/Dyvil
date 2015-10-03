package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class ParserUtil
{
	// Character Utilities
	
	public static boolean isOpenBracket(char c)
	{
		switch (c)
		{
		case '(':
		case '[':
		case '{':
			return true;
		}
		return false;
	}
	
	public static boolean isCloseBracket(char c)
	{
		switch (c)
		{
		case ')':
		case ']':
		case '}':
			return true;
		}
		return false;
	}
	
	public static boolean isBinDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
			return true;
		}
		return false;
	}
	
	public static boolean isOctDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
			return true;
		}
		return false;
	}
	
	public static boolean isDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return true;
		}
		return false;
	}
	
	public static boolean isHexDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
			return true;
		}
		return false;
	}
	
	public static boolean isIdentifierPart(char c)
	{
		if (c <= 0xA0)
		{
			return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
		}
		
		return Character.isUnicodeIdentifierPart(c);
	}
	
	public static boolean isIdentifierSymbol(char c)
	{
		if (c <= 0xA0)
		{
			switch (c)
			{
			case '.':
			case '=':
			case '>':
			case '<':
			case '+':
			case '-':
			case '*':
			case '/':
			case '!':
			case '@':
			case '#':
			case '%':
			case '^':
			case '&':
			case '~':
			case '?':
			case '|':
			case '\\':
			case ':':
				return true;
			}
			
			return false;
		}
		
		switch (Character.getType(c))
		{
		case Character.CONNECTOR_PUNCTUATION:
		case Character.DASH_PUNCTUATION:
		case Character.END_PUNCTUATION:
		case Character.START_PUNCTUATION:
		case Character.OTHER_PUNCTUATION:
		case Character.CURRENCY_SYMBOL:
		case Character.MATH_SYMBOL:
			return true;
		}
		return false;
	}
	
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
