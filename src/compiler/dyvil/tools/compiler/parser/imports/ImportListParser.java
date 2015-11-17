package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.header.IImportList;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ImportListParser extends Parser
{
	protected IImportList theImport;
	
	public ImportListParser(IImportList list)
	{
		this.theImport = list;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.CLOSE_CURLY_BRACKET || type == BaseSymbols.SEMICOLON)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			pm.pushParser(new ImportParser(this.theImport::addImport), true);
			this.mode = 1;
			return;
		}
		if (this.mode == 1)
		{
			this.mode = 0;
			if (type != BaseSymbols.COMMA)
			{
				pm.reparse();
				pm.report(token, "Invalid Import List - ',' expected");
			}
			return;
		}
	}
}
