package dyvil.tools.compiler.parser.imports;

import java.util.function.Consumer;

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
	public static final int		DOT_ALIAS	= 2;
	public static final int		MULTIIMPORT	= 4;
	
	protected Consumer<IImport>	consumer;
	protected IImport			theImport;
	
	public ImportParser(Consumer<IImport> consumer)
	{
		this.consumer = consumer;
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
			this.consumer.accept(this.theImport);
			pm.popParser();
			return;
		}
		if (type == Symbols.COMMA || this.mode == 0)
		{
			this.consumer.accept(this.theImport);
			pm.popParser(true);
			return;
		}
		if (this.mode == IMPORT)
		{
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				MultiImport mi = new MultiImport(token);
				mi.setParent(this.theImport);
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
				pi.setParent(this.theImport);
				this.theImport = pi;
				this.mode = 0;
				return;
			}
			if (type == Keywords.ANNOTATION)
			{
				SimpleImport si = new SimpleImport(token.raw(), annotation);
				si.setParent(this.theImport);
				this.theImport = si;
				this.mode = DOT_ALIAS;
				return;
			}
			if (type == Keywords.TYPE)
			{
				SimpleImport si = new SimpleImport(token.raw(), ImportParser.type);
				si.setParent(this.theImport);
				this.theImport = si;
				this.mode = DOT_ALIAS;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				SimpleImport si = new SimpleImport(token.raw(), token.nameValue());
				si.setParent(this.theImport);
				this.theImport = si;
				this.mode = DOT_ALIAS;
				return;
			}
		}
		if (this.mode == DOT_ALIAS)
		{
			if (type == Symbols.DOT)
			{
				this.mode = IMPORT;
				return;
			}
			if (type == Symbols.ARROW_OPERATOR || type == Keywords.AS)
			{
				this.mode = 0;
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					this.theImport.setAlias(next.nameValue());
					pm.skip();
					return;
				}
				
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
	}
}
