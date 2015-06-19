package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.IBounded;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
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
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
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
				this.mode = ARRAY_END;
				ArrayType at = new ArrayType();
				this.type = at;
				pm.pushParser(new TypeParser(at));
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
			if (type == Keywords.VAR)
			{
				this.typed.setType(Types.UNKNOWN);
				pm.popParser();
				return;
			}
			if (type == Keywords.NULL)
			{
				this.typed.setType(Types.NULL);
				pm.popParser();
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
				
				this.type = new Type(token.raw(), token.nameValue());
				this.typed.setType(this.type);
				pm.popParser();
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
		if (this.mode == TUPLE_END)
		{
			this.typed.setType(this.type);
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
		if (this.mode == LAMBDA_TYPE)
		{
			pm.pushParser(new TypeParser((LambdaType) this.type));
			this.mode = LAMBDA_END;
			return;
		}
		if (this.mode == LAMBDA_END)
		{
			this.type.expandPosition(token.prev());
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		}
		if (this.mode == ARRAY_END)
		{
			this.type.expandPosition(token);
			this.typed.setType(this.type);
			pm.popParser();
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Array Type - ']' expected", true);
		}
		if (this.mode == GENERICS)
		{
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			
			this.type.expandPosition(token.prev());
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		}
		if (this.mode == WILDCARD_TYPE)
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
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		}
		if (this.mode == GENERICS_END)
		{
			this.typed.setType(this.type);
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
