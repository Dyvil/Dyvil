package dyvil.tools.compiler.parser.method;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.method.ThrowsDecl;

public class ThrowsDeclParser extends Parser
{	
	private IThrower thrower;
	
	public ThrowsDeclParser(IThrower thrower)
	{
		this.thrower = thrower;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if ("{".equals(value) || ";".equals(value))
		{
			jcp.popParser(token);
			return true;
		}
		else if (",".equals(token))
		{
			return true;
		}
		else if (token.type() == IToken.TYPE_IDENTIFIER)
		{
			this.thrower.addThrows(new ThrowsDecl(value));
			return true;
		}
		return false;
	}
}
