package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public interface IParserManager
{
	public void skip();
	
	public void skip(int n);
	
	public void reparse();
	
	public void jump(IToken token);
	
	public void setParser(Parser parser);
	
	public Parser getParser();
	
	public void pushParser(Parser parser);
	
	public void pushParser(Parser parser, boolean reparse);
	
	public void popParser();
	
	public void popParser(boolean reparse) throws SyntaxError;
}
