package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public class BytecodeParser extends Parser
{
	public IContext	context;
	public Bytecode	bytecode;
	
	public BytecodeParser(IContext context, Bytecode bytecode)
	{
		this.context = context;
		this.bytecode = bytecode;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		
		
		pm.popParser(true);
		return true;
	}
}
