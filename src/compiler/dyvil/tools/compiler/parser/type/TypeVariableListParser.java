package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;

public class TypeVariableListParser extends Parser
{
	protected IGeneric	generic;
	
	public TypeVariableListParser(IGeneric generic)
	{
		this.generic = generic;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new TypeVariableParser(this.generic), true);
			return true;
		}
		if (this.mode == 1)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return true;
			}
			if (ParserUtil.isSeperator(type))
			{
				this.mode = 0;
				return true;
			}
		}
		return false;
	}
}
