package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;

public class BytecodeParser extends Parser
{
	public Bytecode	bytecode;
	
	public BytecodeParser(Bytecode bytecode)
	{
		this.bytecode = bytecode;
	}
	
	@Override
	public void reset()
	{
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		
	}
}
