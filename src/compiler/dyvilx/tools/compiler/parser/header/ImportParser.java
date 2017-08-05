package dyvilx.tools.compiler.parser.header;

import dyvilx.tools.compiler.ast.consumer.IImportConsumer;
import dyvilx.tools.compiler.ast.imports.*;
import dyvilx.tools.compiler.parser.ParserUtil;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.compiler.transform.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public final class ImportParser extends Parser
{
	public static final Name annotation = Name.fromRaw("annotation");
	public static final Name type       = Name.fromRaw("type");

	private static final int IMPORT           = 1;
	private static final int DOT_ALIAS        = 2;
	private static final int MULTI_IMPORT_END = 4;

	protected IImportConsumer consumer;
	protected IImport         theImport;

	private int masks;

	public ImportParser(IImportConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = IMPORT;
	}

	public ImportParser(IImportConsumer consumer, int masks)
	{
		this(consumer);
		this.masks = masks;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case BaseSymbols.COMMA:
			pm.reparse();
			// Fallthrough
		case Tokens.EOF:
			this.end();
			pm.popParser();
			return;
		}

		switch (this.mode)
		{
		case IMPORT:
			switch (type)
			{
			case BaseSymbols.OPEN_CURLY_BRACKET:
			{
				final MultiImport multiImport = new MultiImport(token);
				multiImport.setParent(this.theImport);
				this.theImport = multiImport;

				if (token.next().type() == BaseSymbols.CLOSE_CURLY_BRACKET)
				{
					// Fast-path; import ... { }
					this.mode = END;
					pm.skip();
					return;
				}

				pm.pushParser(new ImportListParser(multiImport));
				this.mode = MULTI_IMPORT_END;
				return;
			}
			case DyvilSymbols.UNDERSCORE:
			{
				final WildcardImport wildcardImport = new WildcardImport(token.raw());
				wildcardImport.setParent(this.theImport);
				this.theImport = wildcardImport;
				this.mode = END;
				return;
			}
			case Tokens.IDENTIFIER:
			case Tokens.SPECIAL_IDENTIFIER:
			case Tokens.SYMBOL_IDENTIFIER:
			case Tokens.LETTER_IDENTIFIER:
			{
				final SingleImport singleImport = new SingleImport(token.raw(), token.nameValue());
				singleImport.setParent(this.theImport);
				this.theImport = singleImport;
				this.mode = DOT_ALIAS;
				return;
			}
			}

			final int mask = KindedImport.parseMask(type);
			if (mask != 0)
			{
				this.masks |= mask;
				return;
			}

			pm.report(token, "import.identifier");
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
			}

			return;
		case DOT_ALIAS:
			switch (type)
			{
			case BaseSymbols.DOT:
				this.mode = IMPORT;
				return;
			case Tokens.SYMBOL_IDENTIFIER:
				if (token.nameValue().unqualified.equals(".*"))
				{
					// Handle Java-style wildcard imports gracefully
					pm.report(Markers.syntaxWarning(token, "import.wildcard.java"));

					final WildcardImport wildcardImport = new WildcardImport(token.raw());
					wildcardImport.setParent(this.theImport);
					this.theImport = wildcardImport;
					this.mode = END;
					return;
				}
				break; // create an error
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
			case DyvilKeywords.AS:
				this.mode = END;
				final IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					this.theImport.setAlias(next.nameValue());
					pm.skip();
					return;
				}
				pm.report(next, "import.alias.identifier");
				return;
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				this.end();
				pm.popParser(true);
				return;
			}
			pm.report(token, "import.dot");
			return;
		case MULTI_IMPORT_END:
			Util.expandPosition(this.theImport, token);
			this.end();
			pm.popParser();
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "import.multi.close_brace");
			}
			return;
		case END:
			this.end();
			pm.popParser(true);
		}
	}

	private void end()
	{
		if (this.masks != 0)
		{
			this.consumer.setImport(new KindedImport(this.theImport, this.masks));
			return;
		}
		this.consumer.setImport(this.theImport);
	}

	@Override
	public boolean reportErrors()
	{
		return true;
	}
}
