package dyvil.tools.parsing.lexer;

public interface Symbols
{
	public int getKeywordType(String value);
	
	public int getSymbolType(String value);
	
	public String toString(int type);
	
	public int getLength(int type);
}
