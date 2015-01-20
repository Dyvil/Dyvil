package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.IImportContainer;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ImportListParser extends Parser
{
	public IImport			parent;
	public IImportContainer	container;
	
	public ImportListParser(IImport parent, IImportContainer container)
	{
		this.parent = parent;
		this.container = container;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isTerminator(type))
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
		if (this.mode == 1)
		{
			if (type == Tokens.COMMA)
			{
				this.mode = 0;
				return true;
			}
		}
		return false;
	}
}
