package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.method.ThrowsDecl;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ThrowsDeclParser extends Parser
{	
	private IThrower thrower;
	
	public ThrowsDeclParser(IThrower thrower)
	{
		this.thrower = thrower;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if ("{".equals(value) || ";".equals(value))
		{
			if (this.mode == 1)
			{
				jcp.popParser(token);
				return true;				
			}
			throw new SyntaxError("Invalid throws delcaration!");
		}
		else if (",".equals(token))
		{
			if (this.mode == 1)
			{
				this.mode = 0;
				return true;
			}
			throw new SyntaxError("Invalid comma");
		}
		else if (token.type() == IToken.TYPE_IDENTIFIER)
		{
			this.thrower.addThrows(new ThrowsDecl(value));
			this.mode = 1;
			return true;
		}
		return false;
	}
}
