package dyvil.tools.compiler.parser.type;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.Type;

public class TypeParser extends Parser
{
	public static final int	NAME		= 0;
	public static final int	GENERICS	= 1;
	public static final int	ARRAY		= 2;
	public static final int	ARRAY2		= 3;
	
	protected ITyped		typed;
	
	private int				mode;
	private Type			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(ITyped typed)
	{
		this.typed = typed;
	}
	
	@Override
	public void parse(ParserManager pm, String value, IToken token) throws SyntaxException
	{
		if (this.mode == NAME)
		{
			if (token.next().equals("["))
			{
				this.type = new GenericType(value);
				this.mode = 1;
			}
			else
			{
				this.type = new Type(value);
				this.mode = 2;
			}
			
			if (!token.next().equals("<"))
			{
				pm.popParser();
			}
		}
		else if ("[".equals(value))
		{
			if (this.mode == NAME)
			{
				throw new SyntaxException("Invalid Type: Generics specified before the name!");
			}
			else if (this.mode == ARRAY)
			{
				throw new SyntaxException("Invalid Type: Genrics specified after array dimensions!");
			}
			else if (this.mode == ARRAY2)
			{
				throw new SyntaxException("Misplaced Construct!");
			}
			pm.pushParser(new TypeListParser((GenericType) this.type));
		}
		else if ("]".equals(value))
		{
			if (this.mode == NAME)
			{
				throw new SyntaxException("Invalid Type: Generics specified before the name!");
			}
			else if (this.mode == ARRAY)
			{
				throw new SyntaxException("Invalid Type: Generics specified after array dimensions!");
			}
			else if (this.mode == ARRAY2)
			{
				throw new SyntaxException("Misplaced Construct!");
			}
			
			if (!token.next().equals("<"))
			{
				pm.popParser();
			}
		}
		else if ("<".equals(value))
		{
			if (this.mode == NAME)
			{
				throw new SyntaxException("Invalid Type: Array dimensions specified before the name!");
			}
			
			this.mode = ARRAY2;
			this.arrayDimensions++;
			this.arrayDimensions2++;
		}
		else if (">".equals(value))
		{
			if (this.mode != ARRAY2)
			{
				throw new SyntaxException("Misplaced Construct!");
			}
			
			this.arrayDimensions2--;
			if (this.arrayDimensions2 == 0)
			{
				pm.popParser();
			}
		}
	}
	
	@Override
	public void end(ParserManager pm)
	{
		this.type.setArrayDimensions(this.arrayDimensions);
		this.typed.setType(this.type);
	}
}
