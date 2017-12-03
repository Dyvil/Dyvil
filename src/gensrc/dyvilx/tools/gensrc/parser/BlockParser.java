package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.ast.directive.HashLiteral;
import dyvilx.tools.gensrc.ast.directive.ProcessedText;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class BlockParser extends Parser
{
	private static final int ELEMENT        = 0;
	private static final int DIRECTIVE_NAME = 1;

	private final Template      template;
	private final StatementList directives;

	public BlockParser(StatementList directives)
	{
		this.template = null;
		this.directives = directives;
	}

	public BlockParser(Template template, StatementList directives)
	{
		this.template = template;
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
				this.directives.add(new ProcessedText(token.raw(), token.stringValue()));
				return;
			case BaseSymbols.HASH:
				this.mode = DIRECTIVE_NAME;
				return;
			}
			return;
		case DIRECTIVE_NAME:
			switch (type)
			{
			case GenSrcSymbols.IMPORT:
				pm.pushParser(new ImportDirectiveParser(this.template), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.IF:
				pm.pushParser(new IfDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.FOR:
				pm.pushParser(new ForDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.VAR:
			case GenSrcSymbols.LET:
			case GenSrcSymbols.CONST:
				pm.pushParser(new VarDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case GenSrcSymbols.FUNC:
				pm.pushParser(new FuncDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.pushParser(new ScopeDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			case BaseSymbols.HASH:
				this.directives.add(new HashLiteral(token.raw()));
				this.mode = ELEMENT;
				return;
			}

			if (Tokens.isIdentifier(type))
			{
				pm.pushParser(new CallDirectiveParser(this.directives), true);
				this.mode = ELEMENT;
				return;
			}

			pm.report(token, "directive.identifier");
		}
	}
}
