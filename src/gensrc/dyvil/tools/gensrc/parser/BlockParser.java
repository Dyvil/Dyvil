package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.tools.gensrc.ast.directive.ProcessedLine;
import dyvil.tools.gensrc.lexer.GenSrcSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class BlockParser extends dyvil.tools.parsing.Parser
{
	private static final int ELEMENT        = 0;
	private static final int DIRECTIVE_NAME = 1;

	private final DirectiveList directives;

	public BlockParser(DirectiveList directives)
	{
		this.directives = directives;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case ELEMENT:
			switch (type)
			{
			case Tokens.EOF:
				return;
			case Tokens.STRING:
				this.directives.add(new ProcessedLine(token.stringValue()));
				return;
			case BaseSymbols.HASH:
				this.mode = DIRECTIVE_NAME;
				return;
			}
			return;
		case DIRECTIVE_NAME:
			switch (type)
			{
			case GenSrcSymbols.IF:
				pm.pushParser(new IfDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.FOR:
				pm.pushParser(new ForDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.DEFINE:
			case GenSrcSymbols.UNDEFINE:
			case GenSrcSymbols.LOCAL:
			case GenSrcSymbols.DELETE:
			case GenSrcSymbols.NAME:
				pm.pushParser(new VarDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			}

			pm.pushParser(new DirectiveParser(this.directives), true);
			this.mode = ELEMENT;
			return;
		}
	}
}
