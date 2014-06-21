package dyvil.tools.compiler.parser.classbody;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.member.Implementation;
import dyvil.tools.compiler.ast.member.methods.IImplementable;

public class ImplementationParser extends Parser
{
	private IImplementable implementable;
	
	public ImplementationParser(IImplementable implementable)
	{
		this.implementable = implementable;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		Implementation impl = new Implementation();
		
		if ("}".equals(value))
		{
			jcp.popParser();
		}
		
		this.implementable.setImplementation(impl);
	}
}
