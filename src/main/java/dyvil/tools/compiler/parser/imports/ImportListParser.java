package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.api.IImport;
import dyvil.tools.compiler.ast.api.IImportContainer;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ImportListParser extends Parser
{
	public IImport parent;
	public IImportContainer	container;
	
	public ImportListParser(IImport parent, IImportContainer container)
	{
		this.parent = parent;
		this.container = container;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if ("}".equals(value) || ";".equals(value))
		{
			pm.popParser(true);
			return true;
		}
		if (this.mode == 0)
		{
			pm.pushParser(new ImportParser(this.parent, this.container), true);
			this.mode = 1;
			return true;
		}
		else if (this.mode == 1)
		{
			if (",".equals(value))
			{
				this.mode = 0;
				return true;
			}
		}
		return false;
	}
}
