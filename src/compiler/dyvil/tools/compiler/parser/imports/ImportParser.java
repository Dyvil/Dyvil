package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ImportParser extends Parser
{
	public static final int	IMPORT		= 1;
	public static final int	DOT			= 2;
	public static final int	ALIAS		= 4;
	public static final int	MULTIIMPORT	= 8;
	
	public IImport			parent;
	public IImportContainer	container;
	
	public ImportParser(IImport parent, IImportContainer container)
	{
		this.parent = parent;
		this.container = container;
		this.mode = IMPORT;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			pm.popParser();
			return true;
		}
		if (type == Tokens.COMMA)
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.isInMode(IMPORT))
		{
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				MultiImport mi = new MultiImport(token, this.parent);
				this.container.addImport(mi);
				this.parent = mi;
				this.container = mi;
				
				if (token.next().type() != Tokens.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new ImportListParser(mi, mi));
					this.mode = MULTIIMPORT;
					return true;
				}
				this.mode = 0;
				pm.skip();
				return true;
			}
			if (type == Tokens.WILDCARD)
			{
				PackageImport pi = new PackageImport(token.raw(), this.parent);
				this.container.addImport(pi);
				this.mode = 0;
				return true;
			}
			if (ParserUtil.isIdentifier(type))
			{
				SimpleImport si = new SimpleImport(token.raw(), this.parent, token.value());
				this.container.addImport(si);
				this.parent = si;
				this.container = si;
				this.mode = DOT | ALIAS;
				return true;
			}
		}
		if (this.isInMode(DOT))
		{
			if (type == Tokens.DOT)
			{
				this.mode = IMPORT;
				return true;
			}
		}
		if (this.isInMode(ALIAS))
		{
			if (type == Tokens.ARROW_OPERATOR)
			{
				IToken next = token.next();
				if (next.type() == Tokens.TYPE_IDENTIFIER)
				{
					((SimpleImport) this.parent).setAlias(next.value());
					pm.skip();
					return true;
				}
				
				this.mode = DOT | IMPORT;
				throw new SyntaxError(next, "Invalid Import Alias");
			}
		}
		if (this.isInMode(MULTIIMPORT))
		{
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				this.container.expandPosition(token);
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(true);
		return true;
	}
}
