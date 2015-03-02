package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.TypeVariable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeVariableParser extends Parser implements ITyped
{
	public static final int	NAME			= 1;
	public static final int	TYPE_VARIABLE	= 16;
	
	public static final int	UPPER			= 1;
	public static final int	LOWER			= 2;
	
	public IGeneric			typed;
	
	private byte			boundMode;
	
	private ITypeVariable	variable;
	
	public TypeVariableParser(IGeneric typed)
	{
		this.mode = NAME;
		this.typed = typed;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			this.variable = new TypeVariable(token, token.value());
			this.mode = TYPE_VARIABLE;
			return;
		}
		if (this.mode == TYPE_VARIABLE)
		{
			String value = token.value();
			if (this.boundMode == 0)
			{
				if ("<=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = LOWER;
					return;
				}
				else if (">=".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					this.boundMode = UPPER;
					return;
				}
			}
			else if (this.boundMode == UPPER)
			{
				if ("&".equals(value))
				{
					pm.pushParser(new TypeParser(this));
					return;
				}
			}
			this.typed.addTypeVariable(this.variable);
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.boundMode == UPPER)
		{
			this.variable.addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			this.variable.setLowerBound(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
