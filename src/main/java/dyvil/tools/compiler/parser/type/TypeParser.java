package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeParser extends Parser implements ITyped
{
	public static final int	NAME			= 1;
	public static final int	GENERICS		= 2;
	public static final int	GENERICS_END	= 4;
	public static final int	TYPE_VARIABLE	= 8;
	public static final int	ARRAY			= 16;
	public static final int	TUPLE_END		= 32;
	public static final int	LAMBDA_TYPE		= 64;
	public static final int	LAMBDA_END		= 128;
	
	public static final int	UPPER			= 1;
	public static final int	LOWER			= 2;
	
	public ITyped			typed;
	public boolean			generic;
	
	private byte			boundMode;
	
	private IType			type;
	private int				arrayDimensions;
	private int				arrayDimensions2;
	
	public TypeParser(ITyped typed)
	{
		this.mode = NAME;
		this.typed = typed;
	}
	
	public TypeParser(ITyped typed, boolean generic)
	{
		this.mode = NAME;
		this.typed = typed;
		this.generic = generic;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(NAME))
		{
			if ("(".equals(value))
			{
				TupleType type = new TupleType();
				pm.pushParser(new TypeListParser(type));
				this.type = type;
				this.mode = TUPLE_END;
				return true;
			}
			else if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				// TODO package.class
				if (token.next().equals("<"))
				{
					this.type = new GenericType(token, value);
					this.mode = GENERICS;
					return true;
				}
				else if (this.generic)
				{
					this.type = new TypeVariable(token, value);
					this.mode = TYPE_VARIABLE;
					return true;
				}
				else
				{
					this.type = new Type(token, value);
					this.mode = ARRAY;
					return true;
				}
			}
			else if ("_".equals(value))
			{
				if (this.generic)
				{
					this.type = new TypeVariable(token, value);
					this.mode = TYPE_VARIABLE;
					return true;
				}
				else
				{
					throw new SyntaxError(token, "Invalid generic Wildcard Type");
				}
			}
			return false;
		}
		if (this.isInMode(TUPLE_END))
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
				
				this.type.expandPosition(token);
				pm.popParser();
				return true;
			}
			return false;
		}
		if (this.isInMode(LAMBDA_TYPE))
		{
			pm.pushParser(new TypeParser((LambdaType) this.type));
			this.mode = LAMBDA_END;
			return true;
		}
		if (this.isInMode(LAMBDA_END))
		{
			this.type.expandPosition(token.prev());
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
				this.type.expandPosition(token.prev());
				pm.popParser(true);
				return true;
			}
		}
		if (this.isInMode(GENERICS))
		{
			if ("<".equals(value))
			{
				GenericType generic = (GenericType) this.type;
				generic.setGeneric();
				pm.pushParser(new TypeListParser(generic));
				this.mode = GENERICS_END;
				return true;
			}
			else
			{
				this.type.expandPosition(token.prev());
				pm.popParser(true);
				return true;
			}
		}
		if (this.isInMode(TYPE_VARIABLE))
		{
			if (this.boundMode == 0)
			{
				if ("<=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = LOWER;
					return true;
				}
				else if (">=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = UPPER;
					return true;
				}
			}
			else if (this.boundMode == UPPER)
			{
				if ("&".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					return true;
				}
			}
			pm.popParser(true);
			return true;
		}
		if (this.isInMode(GENERICS_END))
		{
			if (">".equals(value))
			{
				this.mode = ARRAY;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
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
			((ITypeVariable) this.type).addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			((ITypeVariable) this.type).setLowerBound(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
