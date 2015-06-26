package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IImportList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;

public class ImportListParser extends Parser
{
	protected IImportList	theImport;
	
	public ImportListParser(IImportList list)
	{
		this.theImport = list;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.CLOSE_CURLY_BRACKET || type == Symbols.SEMICOLON)
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
			if (type == Symbols.COMMA)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Import List - ',' expected", true);
		}
	}
}
