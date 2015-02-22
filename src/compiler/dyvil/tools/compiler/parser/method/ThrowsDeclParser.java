package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.method.IThrower;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ParserUtil;

public class ThrowsDeclParser extends Parser implements ITyped
{
	protected IThrower	thrower;
	
	public ThrowsDeclParser(IThrower thrower)
	{
		this.thrower = thrower;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			pm.pushParser(new TypeParser(this), true);
			this.mode = 1;
			return;
		}
		if (this.mode == 1)
		{
			if (ParserUtil.isSeperator(type))
			{
				this.mode = 0;
				return;
			}
			
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.thrower.addThrows(type);
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
}
