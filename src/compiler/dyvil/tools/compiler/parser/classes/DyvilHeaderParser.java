package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.imports.HeaderComponent;
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

public final class DyvilHeaderParser extends Parser
{
	private static final int	PACKAGE	= 1;
	private static final int	IMPORT	= 2;
	
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
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.isInMode(PACKAGE))
		{
			if (type == Keywords.PACKAGE)
			{
				this.mode = IMPORT;
				
				PackageDecl pack = new PackageDecl(token.raw());
				this.unit.setPackageDeclaration(pack);
				pm.pushParser(new PackageParser(pack));
				return;
			}
		}
		if (this.isInMode(IMPORT))
		{
			if (type == Keywords.IMPORT)
			{
				this.mode = IMPORT;
				HeaderComponent i = new HeaderComponent(token.raw());
				this.unit.addImport(i);
				pm.pushParser(new ImportParser(i));
				return;
			}
			if (type == Keywords.USING)
			{
				this.mode = IMPORT;
				HeaderComponent i = new HeaderComponent(token.raw(), true);
				this.unit.addStaticImport(i);
				pm.pushParser(new ImportParser(i));
				return;
			}
			if (type == Keywords.OPERATOR)
			{
				this.mode = IMPORT;
				Operator operator = new Operator(token.next().nameValue());
				this.unit.addOperator(operator);
				pm.skip();
				pm.pushParser(new OperatorParser(operator));
				return;
			}
		}
		throw new SyntaxError(token, "Invalid Token - Delete this token");
	}
}
