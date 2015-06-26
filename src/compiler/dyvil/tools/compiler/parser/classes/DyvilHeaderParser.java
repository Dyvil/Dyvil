package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.IncludeParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;

public class DyvilHeaderParser extends Parser
{
	protected static final int	PACKAGE	= 1;
	protected static final int	IMPORT	= 2;
	
	protected IDyvilHeader		unit;
	
	public DyvilHeaderParser(IDyvilHeader unit)
	{
		this.unit = unit;
		this.mode = PACKAGE | IMPORT;
	}
	
	@Override
	public void reset()
	{
		this.mode = PACKAGE | IMPORT;
	}
	
	protected boolean parsePackage(IParserManager pm, IToken token)
	{
		if (token.type() == Keywords.PACKAGE)
		{
			PackageDeclaration pack = new PackageDeclaration(token.raw());
			this.unit.setPackageDeclaration(pack);
			pm.pushParser(new PackageParser(pack));
			return true;
		}
		return false;
	}
	
	protected boolean parseImport(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Keywords.IMPORT)
		{
			ImportDeclaration i = new ImportDeclaration(token.raw());
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addImport(i);
			}));
			return true;
		}
		if (type == Keywords.USING)
		{
			ImportDeclaration i = new ImportDeclaration(token.raw(), true);
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addUsing(i);
			}));
			return true;
		}
		if (type == Keywords.OPERATOR)
		{
			pm.pushParser(new OperatorParser(this.unit, true), true);
			return true;
		}
		if (type == Keywords.PREFIX || type == Keywords.POSTFIX || type == Keywords.INFIX)
		{
			pm.pushParser(new OperatorParser(this.unit, false), true);
			return true;
		}
		if (type == Keywords.INCLUDE)
		{
			IncludeDeclaration i = new IncludeDeclaration(token.raw());
			pm.pushParser(new IncludeParser(this.unit, i));
			return true;
		}
		return false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (token.type() == Symbols.SEMICOLON)
		{
			return;
		}
		if (this.isInMode(PACKAGE))
		{
			if (this.parsePackage(pm, token))
			{
				this.mode = IMPORT;
				return;
			}
		}
		if (this.isInMode(IMPORT))
		{
			if (this.parseImport(pm, token))
			{
				return;
			}
		}
		throw new SyntaxError(token, "Invalid Token - Delete this token");
	}
}
