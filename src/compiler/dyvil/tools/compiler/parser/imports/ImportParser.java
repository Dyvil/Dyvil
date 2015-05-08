package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class ImportParser extends Parser
{
	public static final Name	annotation	= Name.getQualified("annotation");
	public static final Name	type		= Name.getQualified("type");
	
	public static final int		IMPORT		= 1;
	public static final int		DOT			= 2;
	public static final int		ALIAS		= 4;
	public static final int		MULTIIMPORT	= 8;
	
	protected IImport			theImport;
	
	public ImportParser(IImport container)
	{
		this.theImport = container;
		this.mode = IMPORT;
	}
	
	@Override
	public void reset()
	{
		this.mode = IMPORT;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON)
		{
			pm.popParser();
			return;
		}
		if (type == Symbols.COMMA)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.isInMode(IMPORT))
		{
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				MultiImport mi = new MultiImport(token);
				this.theImport.addImport(mi);
				this.theImport = mi;
				
				if (token.next().type() != Symbols.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new ImportListParser(mi));
					this.mode = MULTIIMPORT;
					return;
				}
				this.mode = 0;
				pm.skip();
				return;
			}
			if (type == Symbols.WILDCARD)
			{
				PackageImport pi = new PackageImport(token.raw());
				this.theImport.addImport(pi);
				this.mode = 0;
				return;
			}
			if (type == Keywords.ANNOTATION)
			{
				SimpleImport si = new SimpleImport(token.raw(), annotation);
				this.theImport.addImport(si);
				this.theImport = si;
				this.mode = DOT | ALIAS;
				return;
			}
			if (type == Keywords.TYPE)
			{
				SimpleImport si = new SimpleImport(token.raw(), ImportParser.type);
				this.theImport.addImport(si);
				this.theImport = si;
				this.mode = DOT | ALIAS;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				SimpleImport si = new SimpleImport(token.raw(), token.nameValue());
				this.theImport.addImport(si);
				this.theImport = si;
				this.mode = DOT | ALIAS;
				return;
			}
		}
		if (this.isInMode(DOT))
		{
			if (type == Symbols.DOT)
			{
				this.mode = IMPORT;
				return;
			}
		}
		if (this.isInMode(ALIAS))
		{
			if (type == Symbols.ARROW_OPERATOR)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					this.theImport.setAlias(token.nameValue());
					pm.skip();
					return;
				}
				
				this.mode = DOT | IMPORT;
				throw new SyntaxError(next, "Invalid Import Alias");
			}
		}
		if (this.isInMode(MULTIIMPORT))
		{
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.theImport.expandPosition(token);
				this.mode = 0;
				return;
			}
		}
		
		pm.popParser(true);
		return;
	}
}
