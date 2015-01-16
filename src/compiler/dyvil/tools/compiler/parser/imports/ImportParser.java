package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.api.IImport;
import dyvil.tools.compiler.ast.api.IImportContainer;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

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
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			pm.popParser();
			return true;
		}
		if (this.isInMode(IMPORT))
		{
			if ("{".equals(value))
			{
				MultiImport mi = new MultiImport(token, this.parent);
				this.container.addImport(mi);
				this.parent = mi;
				this.container = mi;
				
				if (!token.next().equals("}"))
				{
					pm.pushParser(new ImportListParser(mi, mi));
					this.mode = MULTIIMPORT;
					return true;
				}
				this.mode = 0;
				pm.skip();
				return true;
			}
			else if ("_".equals(value))
			{
				PackageImport pi = new PackageImport(token.raw(), this.parent);
				this.container.addImport(pi);
				this.mode = 0;
				return true;
			}
			else if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				SimpleImport si = new SimpleImport(token.raw(), this.parent, value);
				this.container.addImport(si);
				this.parent = si;
				this.container = si;
				this.mode = DOT | ALIAS;
				return true;
			}
		}
		if (this.isInMode(DOT))
		{
			if (".".equals(value))
			{
				this.mode = IMPORT;
				return true;
			}
		}
		if (this.isInMode(ALIAS))
		{
			if ("=>".equals(value))
			{
				IToken next = token.next();
				if (next.isType(IToken.TYPE_IDENTIFIER))
				{
					((SimpleImport) this.parent).setAlias(next.value());
					pm.skip();
					return true;
				}
				else
				{
					this.mode = DOT | IMPORT;
					throw new SyntaxError(next, "Invalid Import Alias");
				}
			}
		}
		if (this.isInMode(MULTIIMPORT))
		{
			if ("}".equals(value))
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
