package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

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
	
	public static int getKeywordType(String s, int type)
	{
		switch (s)
		{
		case "_":
			return Tokens.WILDCARD;
		case ":":
			return Tokens.COLON;
		case "=":
			return Tokens.EQUALS;
		case "#":
			return Tokens.HASH;
		case "=>":
			return Tokens.ARROW_OPERATOR;
		case "null":
			return Tokens.NULL;
		case "true":
			return Tokens.TRUE;
		case "false":
			return Tokens.FALSE;
		case "this":
			return Tokens.THIS;
		case "super":
			return Tokens.SUPER;
		case "new":
			return Tokens.NEW;
		case "return":
			return Tokens.RETURN;
		case "if":
			return Tokens.IF;
		case "else":
			return Tokens.ELSE;
		case "while":
			return Tokens.WHILE;
		case "do":
			return Tokens.DO;
		case "for":
			return Tokens.FOR;
		case "break":
			return Tokens.BREAK;
		case "continue":
			return Tokens.CONTINUE;
		case "goto":
			return Tokens.GOTO;
		case "case":
			return Tokens.CASE;
		case "try":
			return Tokens.TRY;
		case "catch":
			return Tokens.CATCH;
		case "finally":
			return Tokens.FINALLY;
		case "synchronized":
			return Tokens.SYNCHRONIZED;
		case "throw":
			return Tokens.THROW;
		default:
			return type;
		}
	}
	
	public static boolean isIdentifier(int type)
	{
		return (type & Tokens.TYPE_IDENTIFIER) != 0;
	}
	
	public static boolean isCloseBracket(int type)
	{
		return (type & Tokens.CLOSE_BRACKET) == Tokens.CLOSE_BRACKET;
	}
	
	public static boolean isTerminator(int type)
	{
		return type == Tokens.COMMA || type == Tokens.SEMICOLON || (type & Tokens.CLOSE_BRACKET) == Tokens.CLOSE_BRACKET;
	}
	
	public static boolean isTerminator2(int type)
	{
		return type == Tokens.DOT || type == Tokens.COMMA || type == Tokens.SEMICOLON || type == Tokens.EQUALS
				|| (type & Tokens.CLOSE_BRACKET) == Tokens.CLOSE_BRACKET;
	}
	
	public static boolean isSeperator(int type)
	{
		return type == Tokens.COMMA || type == Tokens.SEMICOLON;
	}
	
	public static IValue parsePrimitive(IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.TRUE:
			return new BooleanValue(token.raw(), true);
		case Tokens.FALSE:
			return new BooleanValue(token.raw(), false);
		case Tokens.TYPE_STRING:
			return new StringValue(token.raw(), (String) token.object());
		case Tokens.TYPE_CHAR:
			return new CharValue(token.raw(), (Character) token.object());
		case Tokens.TYPE_INT:
			return new IntValue(token.raw(), (Integer) token.object());
		case Tokens.TYPE_LONG:
			return new LongValue(token.raw(), (Long) token.object());
		case Tokens.TYPE_FLOAT:
			return new FloatValue(token.raw(), (Float) token.object());
		case Tokens.TYPE_DOUBLE:
			return new DoubleValue(token.raw(), (Double) token.object());
		}
		return null;
	}
}
