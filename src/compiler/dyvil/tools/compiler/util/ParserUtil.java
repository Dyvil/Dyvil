package dyvil.tools.compiler.util;

import static dyvil.tools.compiler.lexer.token.IToken.*;

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
		return c >= '!' && c <= '~' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && !isSymbol(c) && !isOpenBracket(c) && !isCloseBracket(c);
	}
	
	public static boolean isSeperator(char c)
	{
		return c == ',' || c == ';' || c == ':';
	}
	
	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "_":
			return KEYWORD_WC;
		case "@":
			return KEYWORD_AT;
		case "null":
			return KEYWORD_NULL;
		case "true":
			return KEYWORD_TRUE;
		case "false":
			return KEYWORD_FALSE;
		case "this":
			return KEYWORD_THIS;
		case "super":
			return KEYWORD_SUPER;
		case "new":
			return KEYWORD_NEW;
		case "return":
			return KEYWORD_RETURN;
		case "if":
			return KEYWORD_IF;
		case "else":
			return KEYWORD_ELSE;
		case "while":
			return KEYWORD_WHILE;
		case "do":
			return KEYWORD_DO;
		case "for":
			return KEYWORD_FOR;
		case "switch":
			return KEYWORD_SWITCH;
		case "case":
			return KEYWORD_CASE;
		}
		return TYPE_IDENTIFIER;
	}
}
