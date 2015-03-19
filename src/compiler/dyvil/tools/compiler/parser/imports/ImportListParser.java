package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.IImportContainer;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Tokens;

public class ImportListParser extends Parser
{
	protected IImport			parent;
	protected IImportContainer	container;
	
	public ImportListParser(IImport parent, IImportContainer container)
	{
		this.parent = parent;
		this.container = container;
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
		if (type == Symbols.CLOSE_CURLY_BRACKET || type == Tokens.SEMICOLON)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			pm.pushParser(new ImportParser(this.parent, this.container), true);
			this.mode = 1;
			return;
		}
		if (this.mode == 1)
		{
			this.mode = 0;
			if (type == Tokens.COMMA)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Import List - ',' expected", true);
		}
	}
}
