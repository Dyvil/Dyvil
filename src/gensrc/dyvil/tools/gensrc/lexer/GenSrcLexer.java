package dyvil.tools.gensrc.lexer;

import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenList;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.lexer.Lexer;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IdentifierToken;
import dyvil.tools.parsing.token.StringToken;
import dyvil.tools.parsing.token.SymbolToken;

import static dyvil.tools.parsing.lexer.BaseSymbols.*;
import static dyvil.tools.parsing.lexer.Tokens.LETTER_IDENTIFIER;
import static dyvil.tools.parsing.lexer.Tokens.STRING;

public class GenSrcLexer extends dyvil.tools.parsing.lexer.Lexer
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
				if (this.nextCodePoint() != '#')
				{
					break loop;
				}
				this.advance();
				break;
			case '{':
				this.braceLevel++;
				break;
			case '}':
				if (this.braceLevel >= 0 && --this.braceLevel <= 0)
				{
					break loop;
				}
				break;
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

		//noinspection StatementWithEmptyBody
		while (this.parseArguments())
			;
	}

	private boolean parseArguments()
	{
		int current = this.codePoint();

		// if there exists a ( or { after optional whitespace ...
		final int indexOfNonWhite = Util.skipWhitespace(this.code, this.cursor, this.length);
		switch (this.code.codePointAt(indexOfNonWhite))
		{
		case '(':
			this.skipWhitespace(current);

			this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, OPEN_PARENTHESIS, this.line, this.column));
			this.advance();

			this.parseDyvilArguments();

			current = this.codePoint();
			if (current == 0)
			{
				return false;
			}

			assert current == ')';
			this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, CLOSE_PARENTHESIS, this.line, this.column));
			this.advance();
			this.skipNewLine();
			return true;
		case '{':
			this.skipWhitespace(current);

			this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, OPEN_CURLY_BRACKET, this.line, this.column));
			this.advance();
			this.skipNewLine();

			this.parseNestedBlock();

			current = this.codePoint();
			if (current == 0)
			{
				return false;
			}

			assert current == '}';
			this.tokens.append(new SymbolToken(BaseSymbols.INSTANCE, CLOSE_CURLY_BRACKET, this.line, this.column));

			this.advance();
			this.skipNewLine();
			return true;
		case 0:
		default:
			return false;
		}
	}

	private void skipNewLine()
	{
		if (this.codePoint() == '\n')
		{
			this.newLine();
		}
	}

	private void skipWhitespace(int current)
	{
		while (current != 0 && Character.isWhitespace(current))
		{
			if (current == '\n')
			{
				this.newLine();
			}
			else
			{
				this.advance(current);
			}

			current = this.codePoint();
		}
	}

	private void parseIdentifier()
	{
		int startColumn = this.column;
		int startIndex = this.cursor;

		int current = this.codePoint();
		while (current != 0 && LexerUtil.isIdentifierPart(current))
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
		final DyvilLexer sublexer = new DyvilLexer(this.markers, GenSrcSymbols.INSTANCE);
		sublexer.setInterpolationEnd();

		this.useSubLexer(sublexer);
	}

	private void parseNestedBlock()
	{
		final GenSrcLexer sublexer = new GenSrcLexer(this.markers, this.blockLevel + 1);
		this.useSubLexer(sublexer);
	}

	private void useSubLexer(Lexer sublexer)
	{
		final TokenList tokens = sublexer.tokenize(this.code, this.cursor, this.line, this.column);
		this.tokens.addAll(tokens);

		this.cursor = sublexer.getCursor();
		this.line = sublexer.getLine();
		this.column = sublexer.getColumn();
	}
}
