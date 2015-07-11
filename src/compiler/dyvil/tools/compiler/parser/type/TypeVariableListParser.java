package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;

public class TypeVariableListParser extends Parser
{
	protected IGeneric	generic;
	
	public TypeVariableListParser(IGeneric generic)
	{
		this.generic = generic;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(pm.newTypeVariableParser(this.generic), true);
			return;
		}
		if (this.mode == 1)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = 0;
			if (ParserUtil.isSeperator(type))
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Type Variable List - ',' expected", true);
		}
	}
}
