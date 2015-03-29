package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;

public final class InternalTypeParser extends Parser
{
	private static final int	NAME	= 1;
	private static final int	SLASH	= 2;
	
	protected IInternalTyped	typed;
	
	private StringBuilder		builder;
	
	public InternalTypeParser(IInternalTyped typed)
	{
		this.typed = typed;
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				Object o = parsePrimitive(name);
				if (o != null)
				{
					this.typed.setInternalType(name.qualified, o);
					pm.popParser();
					return;
				}
				
				if (this.builder == null)
				{
					this.builder = new StringBuilder(name.qualified);
				}
				else
				{
					this.builder.append(name.qualified);
				}
				this.mode = SLASH;
				return;
			}
			throw new SyntaxError(token, "Invalid Type - Identifier expected");
		}
		if (this.mode == SLASH)
		{
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (name == Name.div)
				{
					this.builder.append('/');
					this.mode = NAME;
					return;
				}
			}
			
			String s = this.builder.toString();
			this.typed.setInternalType(s, s);
			pm.popParser(true);
			return;
		}
	}
	
	private Object parsePrimitive(Name name)
	{
		if (name == Name._void)
		{
			return ClassFormat.NULL;
		}
		if (name == Name._boolean)
		{
			return ClassFormat.BOOLEAN;
		}
		if (name == Name._byte)
		{
			return ClassFormat.BYTE;
		}
		if (name == Name._short)
		{
			return ClassFormat.SHORT;
		}
		if (name == Name._char)
		{
			return ClassFormat.CHAR;
		}
		if (name == Name._int)
		{
			return ClassFormat.INT;
		}
		if (name == Name._long)
		{
			return ClassFormat.LONG;
		}
		if (name == Name._float)
		{
			return ClassFormat.FLOAT;
		}
		if (name == Name._double)
		{
			return ClassFormat.DOUBLE;
		}
		return null;
	}
}
