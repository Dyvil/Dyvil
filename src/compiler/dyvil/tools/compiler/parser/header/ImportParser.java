package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.consumer.IImportConsumer;
import dyvil.tools.compiler.ast.header.IImport;
import dyvil.tools.compiler.ast.header.MultiImport;
import dyvil.tools.compiler.ast.header.SingleImport;
import dyvil.tools.compiler.ast.header.WildcardImport;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class ImportParser extends Parser
{
	public static final Name annotation = Name.getQualified("annotation");
	public static final Name type       = Name.getQualified("type");

	private static final int IMPORT           = 1;
	private static final int DOT_ALIAS        = 2;
	private static final int MULTI_IMPORT_END = 4;

	protected IImportConsumer consumer;
	protected IImport         theImport;

	public ImportParser(IImportConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = IMPORT;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (type == Tokens.EOF)
		{
			this.consumer.setImport(this.theImport);
			pm.popParser();
			return;
		}
		if (type == BaseSymbols.COMMA || this.mode == END)
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
				final MultiImport multiImport = new MultiImport(token);
				multiImport.setParent(this.theImport);
				this.theImport = multiImport;
				if (token.next().type() != BaseSymbols.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new ImportListParser(multiImport));
					this.mode = MULTI_IMPORT_END;
					return;
				}
				this.mode = END;
				pm.skip();
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
				this.consumer.setImport(this.theImport);
				pm.popParser(true);
				return;
			}
			pm.report(token, "import.dot");
			return;
		case MULTI_IMPORT_END:
			this.theImport.expandPosition(token);
			this.consumer.setImport(this.theImport);
			pm.popParser();
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "import.multi.close_brace");
			}
			return;
		}
	}

	@Override
	public boolean reportErrors()
	{
		return true;
	}
}
