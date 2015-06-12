package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Symbols;

public final class DyvilUnitParser extends DyvilHeaderParser
{
	private static final int	CLASS	= 4;
	
	public DyvilUnitParser(IDyvilHeader unit)
	{
		super(unit);
		this.mode = PACKAGE | IMPORT | CLASS;
	}
	
	@Override
	public void reset()
	{
		this.mode = PACKAGE | IMPORT | CLASS;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.isInMode(PACKAGE))
		{
			if (this.parsePackage(pm, token))
			{
				this.mode = IMPORT | CLASS;
				return;
			}
		}
		if (this.isInMode(IMPORT))
		{
			if (this.parseImport(pm, token))
			{
				this.mode = IMPORT | CLASS;
				return;
			}
		}
		if (this.isInMode(CLASS))
		{
			if (type == Symbols.SEMICOLON)
			{
				return;
			}
			
			this.mode = CLASS;
			CodeClass c = new CodeClass(null, this.unit);
			pm.pushParser(new ClassDeclarationParser(this.unit, c), true);
			return;
		}
		throw new SyntaxError(token, "Invalid Token - Delete this token");
	}
}
