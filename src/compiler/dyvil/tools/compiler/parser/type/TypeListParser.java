package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class TypeListParser extends Parser
{
	protected ITypeConsumer consumer;
	
	public TypeListParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON && token.isInferred() || type == Symbols.OPEN_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case 0:
			this.mode = 1;
			pm.pushParser(pm.newTypeParser(this.consumer), true);
			return;
		case 1:
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = 0;
			if (ParserUtil.isSeperator(type))
			{
				return;
			}
			pm.report(token, "Invalid Type List - ',' expected");
			return;
		}
	}
}
