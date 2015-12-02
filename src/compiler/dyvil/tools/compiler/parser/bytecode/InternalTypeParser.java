package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.token.IToken;

public final class InternalTypeParser extends Parser
{
	private static final int NAME  = 1;
	private static final int SLASH = 2;
	
	protected IInternalTyped typed;
	
	private StringBuilder builder;
	
	public InternalTypeParser(IInternalTyped typed)
	{
		this.typed = typed;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
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
			pm.report(token, "Invalid Type - Identifier expected");
			return;
		case SLASH:
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (name == Names.div)
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
		return name == Names._void || name == Names._boolean || name == Names._byte || name == Names._short
				|| name == Names._char || name == Names._int || name == Names._long || name == Names._float
				|| name == Names._double;
	}
}
