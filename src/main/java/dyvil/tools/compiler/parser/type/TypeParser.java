package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeParser extends Parser
{
	public static final int	NAME		= 1;
	public static final int	GENERICS	= 2;
	public static final int	ARRAY		= 4;
	
	protected ITyped		typed;
	
	private Type			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(ITyped typed)
	{
		this.mode = NAME;
		this.typed = typed;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(NAME))
		{
			if ("<".equals(value))
			{
				this.mode = GENERICS;
				return true;
			}
			else if ("[".equals(value))
			{
				this.mode = ARRAY;
				this.arrayDimensions++;
				this.arrayDimensions2++;
				return true;
			}
			else if (token.isType(Token.TYPE_IDENTIFIER))
			{
				this.mode = GENERICS | ARRAY;
				this.type = new Type(value);
				return true;
			}
			else
			{
				pm.popParser(token);
				return true;
			}
		}
		if (this.isInMode(ARRAY))
		{
			if ("]".equals(value))
			{
				this.arrayDimensions2--;
				if (this.arrayDimensions2 == 0)
				{
					pm.popParser();
				}
				return true;
			}
			else if ("[".equals(value))
			{
				this.arrayDimensions++;
				this.arrayDimensions2++;
				return true;
			}
			else
			{
				pm.popParser(token);
				return true;
			}
		}
		if (this.isInMode(GENERICS))
		{
			if (">".equals(value))
			{
				this.mode = ARRAY;
				return true;
			}
			else
			{
				// TODO Generics
			}
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		this.type.setArrayDimensions(this.arrayDimensions);
		this.typed.setType(this.type);
	}
}
