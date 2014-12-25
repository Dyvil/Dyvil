package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeParser extends Parser
{
	public static final int	NAME		= 1;
	public static final int	GENERICS	= 2;
	public static final int	ARRAY		= 4;
	public static final int	TUPLE_TYPE	= 8;
	public static final int	LAMBDA_TYPE	= 16;
	public static final int	LAMBDA_END	= 32;
	
	protected IContext		context;
	protected ITyped		typed;
	
	private Type			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(IContext context, ITyped typed)
	{
		this.mode = NAME;
		this.context = context;
		this.typed = typed;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(NAME))
		{
			if ("(".equals(value))
			{
				TupleType type = new TupleType();
				pm.pushParser(new TypeListParser(this.context, type));
				this.type = type;
				this.mode = TUPLE_TYPE;
				return true;
			}
			else if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				// TODO package.class
				this.type = new Type(token, value);
				
				IToken next = token.next();
				if (next.equals("<"))
				{
					this.mode = GENERICS;
				}
				else if (next.equals("["))
				{
					this.mode = ARRAY;
				}
				else
				{
					pm.popParser();
				}
				
				return true;
			}
		}
		if (this.isInMode(TUPLE_TYPE))
		{
			if (")".equals(value))
			{
				if (token.next().equals("=>"))
				{
					TupleType tupleType = (TupleType) this.type;
					this.type = new LambdaType(tupleType);
					this.mode = LAMBDA_TYPE;
					return true;
				}
				
				pm.popParser();
				return true;
			}
		}
		if (this.isInMode(LAMBDA_TYPE))
		{
			pm.pushParser(new TypeParser(this.context, (LambdaType) this.type));
			this.mode = LAMBDA_END;
			return true;
		}
		if (this.isInMode(LAMBDA_END))
		{
			pm.popParser(true);
			return true;
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
				pm.popParser(true);
				return true;
			}
		}
		if (this.isInMode(GENERICS))
		{
			if ("<".equals(value))
			{
				return true;
			}
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
		if (this.type != null)
		{
			if (this.type instanceof PrimitiveType || this.type == Type.OBJECT || this.type == Type.STRING)
			{
				this.type = this.type.clone();
			}
			this.type.setArrayDimensions(this.arrayDimensions);
		}
		this.typed.setType(this.type);
	}
}
