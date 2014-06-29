package dyvil.tools.compiler.parser.codeblock;

import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class CodeBlockParser extends Parser
{
	private IImplementable implementable;
	
	public CodeBlockParser(IImplementable implementable)
	{
		this.implementable = implementable;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		// TODO
		return false;
	}
}
