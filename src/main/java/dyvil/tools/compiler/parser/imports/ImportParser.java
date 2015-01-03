package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.api.IImport;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ImportParser extends Parser
{
	public static final int		PACKAGEIMPORT		= 1;
	public static final int		MULTIIMPORT_START	= 2;
	public static final int		ALIAS				= 4;
	
	protected CompilationUnit	unit;
	
	private IImport				theImport;
	private StringBuilder		buffer				= new StringBuilder();
	
	public ImportParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			if (this.theImport instanceof SimpleImport)
			{
				((SimpleImport) this.theImport).setImport(this.buffer.toString());
			}
			this.theImport.expandPosition(token.prev());
			pm.popParser();
			return true;
		}
		else if (this.mode == 0)
		{
			if ("{".equals(value))
			{
				this.mode = MULTIIMPORT_START;
				this.theImport = new MultiImport(token, this.buffer.toString());
				this.buffer.delete(0, this.buffer.length());
				return true;
			}
			else if ("_".equals(value))
			{
				this.theImport = new PackageImport(token, this.buffer.toString());
				return true;
			}
			else if (".".equals(value))
			{
				IToken next = token.next();
				if (!next.equals("_") && !next.equals("{"))
				{
					this.buffer.append(value);
				}
				return true;
			}
			
			if (this.theImport == null)
			{
				this.theImport = new SimpleImport(token);
			}
			
			if (":".equals(value) && token.prev().isType(IToken.TYPE_IDENTIFIER))
			{
				this.mode = ALIAS;
				return true;
			}
			
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				this.buffer.append(value);
				return true;
			}
		}
		else if (this.mode == MULTIIMPORT_START)
		{
			if (",".equals(value))
			{
				if (this.buffer.length() > 0)
				{
					((MultiImport) this.theImport).addClass(this.buffer.toString());
					this.buffer.delete(0, this.buffer.length());
					return true;
				}
			}
			else if ("}".equals(value))
			{
				this.mode = -1;
				
				if (this.buffer.length() > 0)
				{
					((MultiImport) this.theImport).addClass(this.buffer.toString());
				}
				return true;
			}
			else if (token.isType(IToken.TYPE_IDENTIFIER) || ".".equals(value))
			{
				this.buffer.append(value);
				return true;
			}
		}
		else if (this.isInMode(ALIAS))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				((SimpleImport) this.theImport).setAlias(value);
				this.mode = -1;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.theImport != null)
		{
			this.unit.addImport(this.theImport);
		}
	}
}
