package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ThrowsDeclParser extends Parser implements ITyped
{
	protected IThrower	thrower;
	protected IContext	context;
	
	private Type		type;
	
	public ThrowsDeclParser(IThrower thrower, IContext context)
	{
		this.thrower = thrower;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new TypeParser(this.context, this), true);
			return true;
		}
		if (this.mode == 1)
		{
			if (",".equals(value) || ";".equals(value))
			{
				this.thrower.addThrows(this.type);
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(true);
		this.thrower.addThrows(this.type);
		return true;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
}
