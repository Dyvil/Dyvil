package dyvil.tools.parsing.lexer;

import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.*;

import static dyvil.tools.parsing.lexer.BaseSymbols.*;
import static dyvil.tools.parsing.lexer.Tokens.*;

public final class DyvilLexer
{
	private MarkerList markers;
	private Symbols    symbols;

	private String        code;
	private int           length;
	private TokenIterator tokens;

	private StringBuilder buffer = new StringBuilder();

	private int cursor;
	private int column;
	private int line = 1;
	private int stringParens;

	public DyvilLexer(MarkerList markers, Symbols symbols)
	{
		this.markers = markers;
		this.symbols = symbols;
	}

	public TokenIterator tokenize(String code)
	{
		this.tokens = new TokenIterator();
		this.code = code;
		this.length = code.length();

		while (true)
		{
			final int currentChar = this.codePoint();
			if (currentChar == 0)
			{
				break;
			}

			this.parseCharacter(currentChar);
		}

		this.tokens.append(new EndToken(this.cursor, this.line));
		this.tokens.reset();
		return this.tokens;
	}

	// Parsing

	private void parseCharacter(int currentChar)
	{
		switch (currentChar)
		{
		case '`':
			this.parseBacktickIdentifier();
			return;
		case '"':
			this.parseDoubleString(false);
			return;
		case '\'':
			this.parseSingleString();
			return;
		case '@':
			switch (this.nextCodePoint())
			{
			case '"':
				this.parseVerbatimString();
				return;
			case '\'':
				this.parseVerbatimChar();
				return;
			}

			this.parseIdentifier('@', MOD_SYMBOL);
			return;
		case '/':
			switch (this.nextCodePoint())
			{
			case '*':
				this.parseBlockComment();
				return;
			case '/':
				this.parseLineComment();
				return;
			}
			this.parseIdentifier('/', MOD_SYMBOL);
			return;
		case '(':
			if (this.stringParens > 0)
			{
				this.stringParens++;
			}
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_PARENTHESIS, this.line, this.advance()));
			return;
		case ')':
			if (this.stringParens > 0 && --this.stringParens == 0)
			{
				this.parseDoubleString(true);
				return;
			}

			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_PARENTHESIS, this.line, this.advance()));
			return;
		case '[':
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_SQUARE_BRACKET, this.line, this.advance()));
			return;
		case ']':
			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_SQUARE_BRACKET, this.line, this.advance()));
			return;
		case '{':
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_CURLY_BRACKET, this.line, this.advance()));
			return;
		case '}':
			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_CURLY_BRACKET, this.line, this.advance()));
			return;
		case '.':
		{
			final int n = this.nextCodePoint();
			if (LexerUtil.isIdentifierSymbol(n) || n == '.')
			{
				this.parseIdentifier('.', MOD_DOT);
				return;
			}
			this.tokens.append(new SymbolToken(INSTANCE, DOT, this.line, this.advance()));
			return;
		}
		case ';':
			this.tokens.append(new SymbolToken(INSTANCE, SEMICOLON, this.line, this.advance()));
			return;
		case ',':
			this.tokens.append(new SymbolToken(INSTANCE, COMMA, this.line, this.advance()));
			return;
		case '_':
		case '$':
			this.parseIdentifier(currentChar, MOD_SYMBOL | MOD_LETTER);
			return;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			this.parseNumberLiteral(currentChar);
			return;
		case '\n':
			this.newLine();
			return;
		case ' ':
		case '\t':
			this.advance();
			return;
		}
		if (LexerUtil.isIdentifierSymbol(currentChar))
		{
			this.parseIdentifier(currentChar, MOD_SYMBOL);
			return;
		}
		if (LexerUtil.isIdentifierPart(currentChar))
		{
			this.parseIdentifier(currentChar, MOD_LETTER);
			return;
		}

		this.advance(currentChar);
	}

	private void parseBacktickIdentifier()
	{
		// assert this.codePoint() == '`';

		final int startIndex = this.advance();
		final int startLine = this.line;

		this.clearBuffer();

		while (true)
		{
			final int currentChar = this.codePoint();
			switch (currentChar)
			{
			case '\n':
				this.newLine();
				continue;
			case '\t':
			case '\b':
				this.advance();
				continue;
			case EOF:
				this.error("identifier.backtick.unclosed");
				// Fallthrough
			case '`':
				if (this.buffer.length() == 0)
				{
					this.error("identifier.backtick.empty");
					this.buffer.append('_');
				}

				this.tokens.append(
					new IdentifierToken(Name.from(this.buffer.toString()), SPECIAL_IDENTIFIER, startLine, startIndex,
					                    this.advance() + 1));
				return;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseSingleString()
	{
		// assert this.codePoint() == '\'';

		final int startColumn = this.advance();

		this.clearBuffer();

		while (true)
		{
			final int currentChar = this.codePoint();
			switch (currentChar)
			{
			case '\\':
				this.parseEscape(this.nextCodePoint());
				continue;
			case '\t':
			case '\b':
				this.advance();
				continue;
			case '\n':
				this.newLine();
				this.error("string.single.newline");
				continue;
			case EOF:
				this.error("string.single.unclosed");
				// Fallthrough
			case '\'':
				this.tokens.append(
					new StringToken(this.buffer.toString(), SINGLE_QUOTED_STRING, this.line, this.line, startColumn,
					                this.advance() + 1));
				return;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseDoubleString(boolean stringPart)
	{
		// assert this.codePoint() == (stringPart ? ')' : '"');

		final int startColumn = this.advance();
		final int startLine = this.line;

		this.clearBuffer();

		while (true)
		{
			final int currentChar = this.codePoint();

			switch (currentChar)
			{
			case '\\':
				final int nextChar = this.nextCodePoint();
				if (nextChar == '(')
				{
					this.advance();
					if (this.stringParens > 0)
					{
						this.error("string.double.interpolation.nested");
						continue; // parse the rest of the string as normal
					}

					this.tokens.append(
						new StringToken(this.buffer.toString(), stringPart ? STRING_PART : STRING_START, startLine,
						                this.line, startColumn, this.advance() + 1));
					this.stringParens = 1;
					return;
				}

				this.parseEscape(nextChar);
				continue;
			case EOF:
				this.error("string.double.unclosed");
				// Fallthrough
			case '"':
				this.tokens.append(
					new StringToken(this.buffer.toString(), stringPart ? STRING_END : STRING, startLine, this.line,
					                startColumn, this.advance() + 1));
				return;
			case '\n':
				this.newLine();
				break;
			case '\t':
				this.advance();
				continue;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseVerbatimString()
	{
		// assert this.codePoint() == '@';
		// assert this.nextCodePoint() == '"';

		final int startColumn = this.column;
		final int startLine = this.line;

		this.advance2();

		this.clearBuffer();

		while (true)
		{
			final int currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				this.error("string.verbatim.unclosed");
				// Fallthrough
			case '"':
				this.tokens.append(
					new StringToken(this.buffer.toString(), VERBATIM_STRING, startLine, this.line, startColumn,
					                this.advance() + 1));
				return;
			case '\n':
				this.newLine();
				continue;
			case '\t':
				this.advance();
				continue;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseVerbatimChar()
	{
		// assert this.codePoint() == '@';
		// assert this.nextCodePoint() == '\'';

		final int startColumn = this.column;
		final int startLine = this.line;

		this.advance2();

		this.clearBuffer();

		int currentChar = this.codePoint();
		switch (currentChar)
		{
		case '\\':
			this.parseEscape(this.nextCodePoint());
			break;
		case '\n':
			this.buffer.append('\n');
			this.newLine();
			break;
		default:
			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}

		while ((currentChar = this.codePoint()) != '\'')
		{
			if (currentChar == 0)
			{
				this.error("char.verbatim.unclosed");
				break;
			}

			this.error("char.verbatim.invalid");
			this.advance(currentChar);
		}

		this.tokens.append(new StringToken(this.buffer.toString(), VERBATIM_CHAR, startLine, this.line, startColumn,
		                                   this.advance() + 1));
	}

	private void parseNumberLiteral(int currentChar)
	{
		final int radix;

		final int startColumn = this.advance();

		this.clearBuffer();
		if (currentChar == '0')
		{
			switch (this.codePoint())
			{
			case 'o':
			case 'O':
				this.advance();
				radix = 8;
				break;
			case 'x':
			case 'X':
				this.advance();
				radix = 16;
				break;
			case 'b':
			case 'B':
				this.advance();
				radix = 2;
				break;
			default:
				this.buffer.append('0');
				radix = 10;
			}
		}
		else
		{
			this.buffer.append((char) currentChar);
			radix = 10;
		}

		byte type = 0; // 0 -> int, 1 -> long, 2 -> float, 3 -> double

		while (true)
		{
			currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				break;
			case '0':
			case '1':
				this.buffer.append((char) currentChar);
				this.advance();
				continue;
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				if (radix >= 8)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				break;
			case '8':
			case '9':
				if (radix >= 10)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				break;
			case 'a':
			case 'b':
			case 'c':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				break;
			case '_':
				this.advance();
				continue;
			case 'd':
			case 'D':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				// Fallthrough
				if (radix >= 10 && !LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.advance();
					type = 3; // double
					continue;
				}
				break;
			case '.':
				if (radix == 10 && LexerUtil.isDigit(this.nextCodePoint()))
				{
					this.buffer.append('.');
					this.advance();
					type = 3; // double
					continue;
				}
				break;
			case 'e':
			case 'E':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				if (radix == 10)
				{
					int n = this.nextCodePoint();
					if (LexerUtil.isDigit(n))
					{
						this.buffer.append((char) currentChar);
						this.advance();
						type = 3; // double
						continue;
					}
					if (n == '-')
					{
						this.buffer.append('e').append('-');
						this.advance();
						type = 3; // double
						continue;
					}
				}
				break;
			case 'f':
			case 'F':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				if (radix >= 10 && !LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.advance();
					type = 2; // float
					continue;
				}
				break;
			case 'l':
			case 'L':
				if (!LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.advance();
					type = 1; // long
					continue;
				}
				break;
			case 'p':
			case 'P':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.advance();
					type = 3; // float
					continue;
				}
				break;
			}

			switch (type)
			{
			case 0: // int
			{
				final IntToken token = new IntToken(0, this.line, startColumn, this.column);
				try
				{
					token.setValue(Integer.parseInt(this.buffer.toString(), radix));
				}
				catch (NumberFormatException ignored)
				{
					this.error(token, "literal.integer.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 1: // long
			{
				final LongToken token = new LongToken(0, this.line, startColumn, this.column);
				try
				{
					token.setValue(Long.parseLong(this.buffer.toString(), radix));
				}
				catch (NumberFormatException ignored)
				{
					this.error(token, "literal.long.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 2: // float
			{
				final FloatToken token = new FloatToken(0, this.line, startColumn, this.column);
				try
				{
					token.setValue(Float.parseFloat(this.buffer.toString()));
				}
				catch (NumberFormatException ignored)
				{
					this.error(token, "literal.float.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 3: // double
			{
				final DoubleToken token = new DoubleToken(0, this.line, startColumn, this.column);
				try
				{
					if (radix == 16)
					{
						this.buffer.insert(0, "0x");
					}
					token.setValue(Double.parseDouble(this.buffer.toString()));
				}
				catch (NumberFormatException ignored)
				{
					this.error(token, "literal.double.invalid");
				}

				this.tokens.append(token);
				return;
			}
			}

			return;
		}
	}

	private void parseLineComment()
	{
		// assert this.codePoint() == '/';
		// assert this.nextCodePoint() == '/';

		this.advance2();

		while (true)
		{
			final int currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				return;
			case '\n':
				this.newLine();
				return;
			}

			this.advance(currentChar);
		}
	}

	private void parseBlockComment()
	{
		// assert this.codePoint() == '/';
		// assert this.nextCodePoint() == '*';

		int level = 1;
		this.advance2();

		while (true)
		{
			final int currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				this.error("comment.block.unclosed");
				return;
			case '\n':
				this.newLine();
				continue;
			case '/':
				if (this.nextCodePoint() == '*')
				{
					level++;
					this.advance2();
					continue;
				}
				this.advance();
				continue;
			case '*':
				if (this.nextCodePoint() == '/')
				{
					level--;
					this.advance2();

					if (level == 0)
					{
						return;
					}
					continue;
				}

				this.advance();
				continue;
			}

			this.advance(currentChar);
		}
	}

	private void parseIdentifier(int currentChar, int subtype)
	{
		final int startColumn = this.column;

		this.clearBuffer();
		this.buffer.appendCodePoint(currentChar);

		this.advance(currentChar);

		while (true)
		{
			currentChar = this.codePoint();

			switch (subtype)
			{
			case MOD_LETTER:
			{
				if (currentChar == '_' || currentChar == '$')
				{
					this.buffer.append((char) currentChar);
					this.advance();
					subtype = MOD_LETTER | MOD_SYMBOL;
					continue;
				}
				if (LexerUtil.isIdentifierPart(currentChar)) // also matches digits
				{
					this.buffer.appendCodePoint(currentChar);
					this.advance(currentChar);
					continue;
				}

				final String id = this.buffer.toString();
				final int keyword = this.symbols.getKeywordType(id);
				if (keyword != 0)
				{
					this.tokens.append(new SymbolToken(this.symbols, keyword, this.line, startColumn));
					return;
				}
				this.tokens.append(
					new IdentifierToken(Name.from(id), Tokens.LETTER_IDENTIFIER, this.line, startColumn, this.column));
				return;
			}
			case MOD_DOT:
				if (currentChar == '.')
				{
					this.buffer.append('.');
					this.advance();
					continue;
				}
				// Fallthrough
			case MOD_SYMBOL:
				if (currentChar == '_' || currentChar == '$')
				{
					this.buffer.append((char) currentChar);
					this.advance();
					subtype = MOD_LETTER | MOD_SYMBOL;
					continue;
				}
				if (LexerUtil.isIdentifierSymbol(currentChar))
				{
					this.buffer.appendCodePoint(currentChar);
					this.advance();
					subtype = MOD_SYMBOL;
					continue;
				}
				break;
			case MOD_LETTER | MOD_SYMBOL:
				if (currentChar == '_' || currentChar == '$')
				{
					this.buffer.append((char) currentChar);
					this.advance();
					continue;
				}
				if (LexerUtil.isIdentifierPart(currentChar))
				{
					this.buffer.appendCodePoint(currentChar);
					this.advance(currentChar);
					subtype = MOD_LETTER;
					continue;
				}
				if (LexerUtil.isIdentifierSymbol(currentChar))
				{
					this.buffer.appendCodePoint(currentChar);
					this.advance(currentChar);
					subtype = MOD_SYMBOL;
					continue;
				}
				break;
			}

			final String id = this.buffer.toString();
			final int symbol = this.symbols.getSymbolType(id);
			if (symbol != 0)
			{
				this.tokens.append(new SymbolToken(this.symbols, symbol, this.line, startColumn));
				return;
			}
			this.tokens.append(
				new IdentifierToken(Name.from(id), Tokens.SYMBOL_IDENTIFIER, this.line, startColumn, this.column));
			return;
		}
	}

	private void parseEscape(int nextChar)
	{
		switch (nextChar)
		{
		case '"':
		case '\'':
		case '\\':
			this.buffer.append((char) nextChar);
			this.advance2();
			return;
		case 'n':
			this.buffer.append('\n');
			this.advance2();
			return;
		case 't':
			this.buffer.append('\t');
			this.advance2();
			return;
		case 'r':
			this.buffer.append('\r');
			this.advance2();
			return;
		case 'b':
			this.buffer.append('\b');
			this.advance2();
			return;
		case 'f':
			this.buffer.append('\f');
			this.advance2();
			return;
		case 'v':
			this.buffer.append('\u000B'); // U+000B VERTICAL TABULATION
			this.advance2();
			return;
		case 'a':
			this.buffer.append('\u0007'); // U+0007 BELL
			this.advance2();
			return;
		case 'e':
			this.buffer.append('\u001B'); // U+001B ESCAPE
			this.advance2();
			return;
		case '0':
			this.buffer.append('\0'); // U+0000 NULL
			this.advance2();
			return;
		case 'u':
		{
			int buf = 0;
			this.advance2();

			if (this.codePoint() != '{')
			{
				this.error("escape.unicode.open_brace");
				return;
			}

			this.advance();
			loop:
			while (true)
			{
				int codePoint = this.codePoint();
				switch (codePoint)
				{
				case '\n':
					this.error("escape.unicode.newline");
					this.newLine();
					continue;
				case ' ':
				case '_':
				case '\t':
					this.advance();
					continue;
				case '}':
					this.advance();
					break loop;
				}
				if (!LexerUtil.isHexDigit(codePoint))
				{
					this.error("escape.unicode.close_brace");
					break;
				}

				buf <<= 4;
				buf += Character.digit(codePoint, 16);
				this.advance();
			}

			this.buffer.appendCodePoint(buf);
			return;
		}
		}

		this.advance();
		this.error("escape.invalid");
		this.advance();
	}

	// Utility Methods

	private int codePoint()
	{
		return this.cursor >= this.length ? 0 : this.code.codePointAt(this.cursor);
	}

	private int nextCodePoint()
	{
		return this.cursor + 1 >= this.length ? 0 : this.code.codePointAt(this.cursor + 1);
	}

	private int advance()
	{
		this.cursor++;
		return this.column++;
	}

	private void advance2()
	{
		this.cursor += 2;
		this.column += 2;
	}

	private void advance(int currentChar)
	{
		this.cursor += Character.charCount(currentChar);
		this.column++;
	}

	private void newLine()
	{
		this.line++;
		this.column = 0;
		this.cursor++;
	}

	private void clearBuffer()
	{
		this.buffer.delete(0, this.buffer.length());
	}

	private void error(String key)
	{
		this.error(new CodePosition(this.line, this.column, this.column + 1), key);
	}

	private void error(ICodePosition position, String key)
	{
		this.markers.add(new SyntaxError(position, this.markers.getI18n().getString(key)));
	}
}
