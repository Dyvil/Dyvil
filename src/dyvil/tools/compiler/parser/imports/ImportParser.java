package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ImportParser extends Parser
{
	protected CompilationUnit	unit;
	
	private int					mode;
	private IImport				theImport;
	private StringBuilder		buffer	= new StringBuilder();
	
	public ImportParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			jcp.popParser();
			return true;
		}
		else if ("{".equals(value))
		{
			if (this.mode == 1)
				throw new SyntaxError("Cannot make nested MultiImports");
			if (this.mode == 2)
				throw new SyntaxError("Cannot make multiple MultiImports at the same time.");
			
			this.mode = 1;
			this.theImport = new MultiImport();
			return true;
		}
		else if ("}".equals(value))
		{
			this.mode = 2;
			return true;
		}
		else if (".".equals(value) && token.next().equals(";"))
		{
			this.theImport = new PackageImport();
			return true;
		}
		else if (this.mode == 1)
		{
			if (!",".equals(value))
			{
				if (token.type() != Token.TYPE_IDENTIFIER)
					throw new SyntaxError("Invalid Import");
				((MultiImport) this.theImport).addClass(value);
				return true;
			}
		}
		else
		{
			this.buffer.append(value);
			return true;
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.theImport == null)
		{
			this.theImport = new SimpleImport(this.buffer.toString());
		}
		else
		{
			((PackageImport) this.theImport).setPackage(this.buffer.toString());
		}
		
		this.unit.addImport(this.theImport);
	}
}
