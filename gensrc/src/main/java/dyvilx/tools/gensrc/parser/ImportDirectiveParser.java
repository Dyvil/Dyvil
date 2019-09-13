package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.imports.KindedImport;
import dyvilx.tools.compiler.parser.header.ImportParser;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class ImportDirectiveParser extends Parser
{
	protected static final int KEYWORD     = 0;
	protected static final int OPEN_PAREN  = 1;
	protected static final int CLOSE_PAREN = 3;

	protected final Template template;

	private ImportDeclaration declaration;

	private int mask = KindedImport.ANY;

	public ImportDirectiveParser(Template template)
	{
		this.template = template;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case KEYWORD:
			switch (type)
			{
			case GenSrcSymbols.USING:
				this.mask = KindedImport.USING_DECLARATION;
				// Fallthrough
			case GenSrcSymbols.IMPORT:
				this.declaration = new ImportDeclaration(token.raw());
				if (this.template == null)
				{
					pm.report(token, "import.context");
				}
				this.mode = OPEN_PAREN;
				return;
			}

			throw new Error();
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.report(token, "import.open_paren");
				pm.popParser(true);
				return;
			}

			pm.pushParser(new ImportParser(this.declaration::setImport, this.mask));
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			switch (type)
			{
			case BaseSymbols.CLOSE_PARENTHESIS:
				if (this.template != null && this.declaration.getImport() != null)
				{
					this.template.addImport(this.declaration);
				}
				pm.popParser();
				return;
			case Tokens.EOF:
				pm.report(token, "import.close_paren");
				pm.popParser();
				return;
			}

			pm.report(token, "import.close_paren");
			return;
		}
	}
}
