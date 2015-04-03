package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.IBounded;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class TypeParser extends Parser implements ITyped
{
	public static final int	NAME			= 1;
	public static final int	GENERICS		= 2;
	public static final int	GENERICS_END	= 4;
	public static final int	ARRAY_END		= 8;
	public static final int	WILDCARD_TYPE	= 16;
	public static final int	TUPLE_END		= 128;
	public static final int	LAMBDA_TYPE		= 256;
	public static final int	LAMBDA_END		= 512;
	
	public static final int	UPPER			= 1;
	public static final int	LOWER			= 2;
	
	protected ITyped		typed;
	
	private byte			boundMode;
	
	private IType			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(ITyped typed)
	{
		this.typed = typed;
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.mode = NAME;
		this.boundMode = 0;
		this.type = null;
		this.arrayDimensions = 0;
		this.arrayDimensions2 = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.isInMode(NAME))
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				TupleType tupleType = new TupleType();
				pm.pushParser(new TypeListParser(tupleType));
				this.type = tupleType;
				this.mode = TUPLE_END;
				return;
			}
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				this.arrayDimensions++;
				this.arrayDimensions2++;
				return;
			}
			if (type == Symbols.ARROW_OPERATOR)
			{
				LambdaType lt = new LambdaType();
				this.type = lt;
				pm.pushParser(new TypeParser(lt));
				this.mode = LAMBDA_END;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				if (token.next().type() == Symbols.OPEN_SQUARE_BRACKET)
				{
					this.type = new GenericType(token, token.nameValue());
					this.mode = GENERICS;
					return;
				}
				
				this.type = new Type(token, token.nameValue());
				this.mode = ARRAY_END;
				return;
			}
			if (type == Symbols.WILDCARD)
			{
				this.type = new WildcardType(token.raw());
				this.mode = WILDCARD_TYPE;
				return;
			}
			
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}
			throw new SyntaxError(token, "Invalid Type - Invalid " + token);
		}
		if (this.isInMode(TUPLE_END))
		{
			this.end();
			pm.popParser();
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				if (token.next().type() == Symbols.ARROW_OPERATOR)
				{
					TupleType tupleType = (TupleType) this.type;
					this.type = new LambdaType(tupleType);
					this.mode = LAMBDA_TYPE;
					return;
				}
				
				this.type.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple Type - ')' expected");
		}
		if (this.isInMode(LAMBDA_TYPE))
		{
			pm.pushParser(new TypeParser((LambdaType) this.type));
			this.mode = LAMBDA_END;
			return;
		}
		if (this.isInMode(LAMBDA_END))
		{
			this.type.expandPosition(token.prev());
			this.end();
			pm.popParser(true);
			return;
		}
		if (this.isInMode(ARRAY_END))
		{
			if (this.arrayDimensions2 > 0)
			{
				if (type == Symbols.CLOSE_SQUARE_BRACKET)
				{
					this.arrayDimensions2--;
					if (this.arrayDimensions2 == 0)
					{
						this.type.expandPosition(token);
						this.end();
						pm.popParser();
						return;
					}
				}
				
				this.type.expandPosition(token.prev());
				this.end();
				pm.popParser(true);
				throw new SyntaxError(token.prev(), "Unclosed array brackets");
			}
			
			this.type.expandPosition(token.prev());
			this.end();
			pm.popParser(true);
			return;
		}
		if (this.isInMode(GENERICS))
		{
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			
			if (this.arrayDimensions2 > 0)
			{
				this.type.expandPosition(token.prev());
				this.end();
				pm.popParser(true);
				throw new SyntaxError(token.prev(), "Invalid Array Type - ']' expected");
			}
			
			this.type.expandPosition(token.prev());
			this.end();
			pm.popParser(true);
			return;
		}
		if (this.isInMode(WILDCARD_TYPE))
		{
			Name name = token.nameValue();
			if (this.boundMode == 0)
			{
				if (name == Name.lteq)
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = LOWER;
					return;
				}
				if (name == Name.gteq)
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = UPPER;
					return;
				}
			}
			else if (this.boundMode == UPPER)
			{
				if (name == Name.amp)
				{
					pm.pushParser(new TypeParser(this));
					return;
				}
			}
			this.end();
			pm.popParser(true);
			return;
		}
		if (this.isInMode(GENERICS_END))
		{
			this.end();
			pm.popParser();
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type - ']' expected", true);
		}
	}
	
	public void end()
	{
		if (this.type != null)
		{
			this.type.setArrayDimensions(this.arrayDimensions);
		}
		this.typed.setType(this.type);
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.boundMode == UPPER)
		{
			((IBounded) this.type).addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			((IBounded) this.type).setLowerBound(type);
		}
		else
		{
			((ITyped) this.type).setType(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
