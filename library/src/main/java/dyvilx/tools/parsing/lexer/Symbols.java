package dyvilx.tools.parsing.lexer;

public interface Symbols
{
	int getKeywordType(String value);
	
	int getSymbolType(String value);
	
	String toString(int type);
	
	int getLength(int type);
}
