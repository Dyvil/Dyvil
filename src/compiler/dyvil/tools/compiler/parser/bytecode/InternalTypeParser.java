package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;

public final class InternalTypeParser extends Parser
{
	private static final int	NAME	= 1;
	private static final int	SLASH	= 2;
	
	protected IInternalTyped typed;
	
	private StringBuilder builder;
	
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
				if (isPrimitiveName(name))
				{
					this.typed.setInternalType(name.qualified);
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
			this.typed.setInternalType(s);
			pm.popParser(true);
			return;
		}
	}
	
	private static boolean isPrimitiveName(Name name)
	{
		return name == Name._void || name == Name._boolean || name == Name._byte || name == Name._short || name == Name._char || name == Name._int
				|| name == Name._long || name == Name._float || name == Name._double;
	}
}
