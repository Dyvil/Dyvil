package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.transform.Keywords;

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
			this.mode = IMPORT;
			
			PackageDecl pack = new PackageDecl(token.raw());
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
			this.mode = IMPORT;
			ImportDeclaration i = new ImportDeclaration(token.raw());
			this.unit.addImport(i);
			pm.pushParser(new ImportParser(i));
			return true;
		}
		if (type == Keywords.USING)
		{
			this.mode = IMPORT;
			ImportDeclaration i = new ImportDeclaration(token.raw(), true);
			this.unit.addStaticImport(i);
			pm.pushParser(new ImportParser(i));
			return true;
		}
		if (type == Keywords.OPERATOR)
		{
			this.mode = IMPORT;
			Operator operator = new Operator(token.next().nameValue());
			this.unit.addOperator(operator);
			pm.skip();
			pm.pushParser(new OperatorParser(operator));
			return true;
		}
		return false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (this.isInMode(PACKAGE))
		{
			if (this.parsePackage(pm, token))
			{
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
