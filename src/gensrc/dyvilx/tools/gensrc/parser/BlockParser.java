package dyvilx.tools.gensrc.parser;

import dyvilx.tools.gensrc.ast.directive.DirectiveList;
import dyvilx.tools.gensrc.ast.directive.ProcessedText;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class BlockParser extends dyvilx.tools.parsing.Parser
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
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser(true);
				return;
			case Tokens.EOF:
				return;
			case Tokens.STRING:
				this.directives.add(new ProcessedText(token.stringValue()));
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
