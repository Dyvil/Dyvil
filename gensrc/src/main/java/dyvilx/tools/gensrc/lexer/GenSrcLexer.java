package dyvilx.tools.gensrc.lexer;

import dyvil.lang.Name;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.CharacterTypes;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.token.IdentifierToken;
import dyvilx.tools.parsing.token.StringToken;
import dyvilx.tools.parsing.token.SymbolToken;

import static dyvilx.tools.parsing.lexer.BaseSymbols.*;
import static dyvilx.tools.parsing.lexer.Tokens.LETTER_IDENTIFIER;
import static dyvilx.tools.parsing.lexer.Tokens.STRING;

public class GenSrcLexer extends dyvilx.tools.parsing.lexer.Lexer
{
	protected       int braceLevel;
	protected final int blockLevel;

	public GenSrcLexer(MarkerList markers)
	{
		this(markers, 0);
	}

	public GenSrcLexer(MarkerList markers, int blockLevel)
	{
		super(markers, GenSrcSymbols.INSTANCE);
		this.blockLevel = blockLevel;
	}

	@Override
	public TokenList tokenize(String code, int cursor, int line, int column)
	{
		this.init(code, cursor, line, column);

		loop:
		while (true)
		{
			final int currentChar = this.codePoint();
			switch (currentChar)
			{
			case 0:
				break loop;
			case '}':
				if (this.braceLevel <= 0)
				{
					break loop;
				}
			}

			this.parseCharacter(currentChar);
		}

		this.finish();
		return this.tokens;
	}

	@Override
	protected void parseCharacter(int i)
	{
		switch (i)
		{
		case '#':
			this.parseDirective();
			break;
		default:
			this.parsePlain();
			break;
		}
	}

	private void parsePlain()
	{
		// skip leading tabs
		for (int i = 0; i < this.blockLevel; i++)
		{
			if (this.codePoint() != '\t')
			{
				break;
			}

			this.advance();
		}

		final int startIndex = this.cursor;
		final int startColumn = this.column;
		final int startLine = this.line;

		loop:
		while (true)
		{
			int current = this.codePoint();

			switch (current)
			{
			case 0:
				break loop;
			case '#':
				break loop;
			case '{':
				this.braceLevel++;
				break;
			case '}':
				if (this.braceLevel == 0)
				{
					// end of block
					break loop;
				}
				this.braceLevel--;
				break;
			case '\r':
				if (this.nextCodePoint() == '\n')
				{
					this.advance();
				}
				// fallthrough
			case '\n':
				// end at newline, but include it
				this.newLine();
				break loop;
			}

			this.advance(current);
		}

		if (startIndex == this.cursor)
		{
			return;
		}

		this.tokens.append(
			new StringToken(this.code.substring(startIndex, this.cursor), STRING, startLine, this.line, startColumn,
			                this.column));
	}

	private void parseDirective()
	{
		this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, BaseSymbols.HASH, this.line, this.column));

		this.advance();

		this.parseIdentifier();

		this.parseArguments();
		this.parseBlock();
	}

	private void parseArguments()
	{
		if (!this.skipTo('('))
		{
			return;
		}

		this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, OPEN_PARENTHESIS, this.line, this.column));
		this.advance();

		this.parseDyvilArguments();

		final int current = this.codePoint();
		if (current == 0)
		{
			return;
		}

		assert current == ')';
		this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, CLOSE_PARENTHESIS, this.line, this.column));
		this.advance();
		this.skipNewLine();
	}

	private void parseBlock()
	{
		if (!this.skipTo('{'))
		{
			return;
		}

		this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, OPEN_CURLY_BRACKET, this.line, this.column));
		this.advance();
		this.skipNewLine();

		this.parseNestedBlock();

		final int current = this.codePoint();
		if (current == 0)
		{
			return;
		}

		assert current == '}';
		this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, CLOSE_CURLY_BRACKET, this.line, this.column));

		this.advance();
		this.skipNewLine();
	}

	private boolean skipTo(int c)
	{
		final int current = this.codePoint();

		final int indexOfNonWhite = skipWhitespace(this.code, this.cursor, this.length);
		if (indexOfNonWhite >= this.length)
		{
			return false;
		}

		if (this.code.codePointAt(indexOfNonWhite) != c)
		{
			return false;
		}

		this.skipWhitespace(current);
		return true;
	}

	private void skipNewLine()
	{
		switch (this.codePoint())
		{
		case '\r':
			if (this.nextCodePoint() == '\n')
			{
				this.advance();
			}
			// fallthrough
		case '\n':
			this.newLine();
			break;
		}
	}

	private void skipWhitespace(int current)
	{
		while (current != 0 && Character.isWhitespace(current))
		{
			switch (current)
			{
			case '\r':
				if (this.nextCodePoint() == '\n')
				{
					this.advance();
				}
				// fallthrough
			case '\n':
				this.newLine();
				break;
			default:
				this.advance(current);
				break;
			}

			current = this.codePoint();
		}
	}

	private void parseIdentifier()
	{
		int startColumn = this.column;
		int startIndex = this.cursor;

		int current = this.codePoint();
		while (current != 0 && CharacterTypes.isIdentifierPart(current))
		{
			this.advance(current);
			current = this.codePoint();
		}

		if (startIndex == this.cursor)
		{
			return;
		}

		final String identifier = this.code.substring(startIndex, this.cursor);
		final int keyWord = this.symbols.getKeywordType(identifier);
		if (keyWord != 0)
		{
			this.tokens.append(new SymbolToken(this.symbols, keyWord, this.line, startColumn));
			return;
		}

		this.tokens.append(
			new IdentifierToken(Name.fromQualified(identifier), LETTER_IDENTIFIER, this.line, startColumn,
			                    this.column));
	}

	private void parseDyvilArguments()
	{
		final DyvilLexer sublexer = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE);
		sublexer.setInterpolationEnd();
		this.useSubLexer(sublexer);
	}

	private void parseNestedBlock()
	{
		final GenSrcLexer sublexer = new GenSrcLexer(this.markers, this.blockLevel + 1);
		this.useSubLexer(sublexer);
	}

	// Utility Methods

	/**
	 * Returns the first index greater than or equal to {@code start} where the character in {@code line} is NOT
	 * whitespace. If no such index is found, {@code end} is returned.
	 *
	 * @param line
	 * 	the string to check
	 * @param start
	 * 	the first index (inclusive) to check
	 * @param end
	 * 	the last index (exclusive) to check
	 *
	 * @return the first index {@code >= start} and {@code < end} where the character in the {@code string} is
	 * non-whitespace, or {@code end}.
	 */
	public static int skipWhitespace(String line, int start, int end)
	{
		for (; start < end; start++)
		{
			if (!Character.isWhitespace(line.charAt(start)))
			{
				return start;
			}
		}
		return end;
	}
}
