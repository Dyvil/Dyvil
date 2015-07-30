package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Symbols;

public final class DyvilUnitParser extends DyvilHeaderParser
{
	private static final int CLASS = 4;
	
	public DyvilUnitParser(IDyvilHeader unit)
	{
		super(unit);
		this.mode = CLASS;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) 
	{
		int type = token.type();
		switch (this.mode)
		{
		case PACKAGE:
			if (this.parsePackage(pm, token))
			{
				this.mode = IMPORT;
				return;
			}
		case IMPORT:
			if (this.parseImport(pm, token))
			{
				this.mode = IMPORT;
				return;
			}
		case CLASS:
			if (type == Symbols.SEMICOLON)
			{
				return;
			}
			
			this.mode = CLASS;
			pm.pushParser(new ClassDeclarationParser(this.unit), true);
			return;
		}
		pm.report(new SyntaxError(token, "Invalid Token - Delete this token")); return;
	}
}
