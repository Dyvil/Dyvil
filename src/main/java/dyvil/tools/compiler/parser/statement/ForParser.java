package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ForParser extends Parser
{
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		return false;
	}
	
}
