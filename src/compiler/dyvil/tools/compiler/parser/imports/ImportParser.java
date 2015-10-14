package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.consumer.IImportConsumer;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.MultiImport;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class ImportParser extends Parser
{
	public static final Name	annotation	= Name.getQualified("annotation");
	public static final Name	type		= Name.getQualified("type");
	
	private static final int	IMPORT		= 1;
	private static final int	DOT_ALIAS	= 2;
	private static final int	MULTIIMPORT	= 4;
	
	protected IImportConsumer	consumer;
	protected IImport			theImport;
	
	public ImportParser(IImportConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = IMPORT;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.SEMICOLON)
		{
			this.consumer.setImport(this.theImport);
			pm.popParser();
			return;
		}
		if (type == BaseSymbols.COMMA || this.mode == 0)
		{
			this.consumer.setImport(this.theImport);
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case IMPORT:
			switch (type)
			{
			case BaseSymbols.OPEN_CURLY_BRACKET:
			{
				MultiImport mi = new MultiImport(token);
				mi.setParent(this.theImport);
				this.theImport = mi;
				if (token.next().type() != BaseSymbols.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new ImportListParser(mi));
					this.mode = MULTIIMPORT;
					return;
				}
				this.mode = 0;
				pm.skip();
				return;
			}
			case DyvilSymbols.WILDCARD:
			{
				PackageImport pi = new PackageImport(token.raw());
				pi.setParent(this.theImport);
				this.theImport = pi;
				this.mode = 0;
				return;
			}
			case Tokens.IDENTIFIER:
			case Tokens.SPECIAL_IDENTIFIER:
			case Tokens.SYMBOL_IDENTIFIER:
			case Tokens.LETTER_IDENTIFIER:
			{
				SimpleImport si = new SimpleImport(token.raw(), token.nameValue());
				si.setParent(this.theImport);
				this.theImport = si;
				this.mode = DOT_ALIAS;
				return;
			}
			}
			pm.popParser();
			pm.report(token, "Invalid Import Declaration - Identifier expected");
			return;
		case DOT_ALIAS:
			switch (type)
			{
			case BaseSymbols.DOT:
				this.mode = IMPORT;
				return;
			case DyvilSymbols.ARROW_OPERATOR:
			case DyvilKeywords.AS:
				this.mode = 0;
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					this.theImport.setAlias(next.nameValue());
					pm.skip();
					return;
				}
				pm.report(next, "Invalid Import Alias");
				return;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				this.consumer.setImport(this.theImport);
				pm.popParser(true);
				return;
			}
			pm.report(token, "Invalid Import Declaration - '.' expected");
			return;
		case MULTIIMPORT:
			this.theImport.expandPosition(token);
			this.consumer.setImport(this.theImport);
			pm.popParser();
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "Invalid Multi-Import - '}' expected");
			}
			return;
		}
	}
}
