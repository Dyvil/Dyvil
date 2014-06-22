package dyvil.tools.compiler.parser.codeblock;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.codeblock.CodeBlock;

public class CodeBlockParser extends Parser
{
	private IImplementable implementable;
	
	public CodeBlockParser(IImplementable implementable)
	{
		this.implementable = implementable;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		CodeBlock impl = new CodeBlock();
		
		if ("}".equals(value))
		{
			jcp.popParser();
		}
		
		this.implementable.setImplementation(impl);
	}
}
