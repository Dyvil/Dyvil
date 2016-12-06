package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.imports.IImportList;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ImportListParser extends Parser
{
	private static final int IMPORT    = 0;
	private static final int SEPARATOR = 1;

	protected IImportList theImport;
	
	public ImportListParser(IImportList list)
	{
		this.theImport = list;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case IMPORT:
			pm.pushParser(new ImportParser(this.theImport::addImport), true);
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser(true);
				return;
			}
			this.mode = IMPORT;
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "import.multi.comma");
			}
			return;
		}
	}
}
