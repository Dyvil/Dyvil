package dyvil.tools.compiler.util;

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
	
	public static boolean isIdentifierPart(char c) {
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
}
