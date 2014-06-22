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
	
	private String exception;
	
	public ThrowsDeclParser(IThrower thrower)
	{
		this.thrower = thrower;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (",".equals(value))
		{
			if (this.exception == null)
			{
				throw new SyntaxException("throwsdecl.invalid.comma");
			}
			this.thrower.addThrowsDecl(new ThrowsDecl(this.exception));
			this.exception = null;
		}
		else
		{
			this.exception = value;
		}
	}
}
