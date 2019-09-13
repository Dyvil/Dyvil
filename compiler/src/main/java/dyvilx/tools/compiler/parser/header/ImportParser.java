package dyvilx.tools.compiler.parser.header;

import dyvilx.tools.compiler.ast.imports.*;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ImportParser extends Parser
{
	// =============== Constants ===============

	private static final int IMPORT           = 1;
	private static final int DOT_ALIAS        = 2;
	private static final int MULTI_IMPORT_END = 4;

	// =============== Fields ===============

	protected Consumer<IImport> consumer;
	protected IImport           theImport;

	private int masks;

	// =============== Constructors ===============

	public ImportParser(Consumer<IImport> consumer)
	{
		this(consumer, 0);
	}

	public ImportParser(Consumer<IImport> consumer, int masks)
	{
		this.consumer = consumer;
		this.masks = masks;
		this.mode = IMPORT;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
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
			if (BaseSymbols.isTerminator(type))
			{
				pm.popParser(type != Tokens.EOF);
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
				if (Tokens.isIdentifier(next.type()))
				{
					this.theImport.setAlias(next.nameValue());
					pm.skip();
					return;
				}
				pm.report(next, "import.alias.identifier");
				return;
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
			case BaseSymbols.CLOSE_PARENTHESIS:
				this.end();
				pm.popParser(true);
				return;
			case Tokens.EOF:
				this.end();
				pm.popParser();
				return;
			}
			pm.report(token, "import.dot");
			return;
		case MULTI_IMPORT_END:
			this.theImport.expandPosition(token);
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
			pm.popParser(type != Tokens.EOF);
		}
	}

	private void end()
	{
		if (this.masks != 0)
		{
			this.consumer.accept(new KindedImport(this.theImport, this.masks));
			return;
		}
		this.consumer.accept(this.theImport);
	}
}
