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
	private int parseIndex;
	private int lineNumber = 1;
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

		while (this.hasNextCodePoint())
		{
			this.parseCharacter(this.codePoint());
		}

		this.tokens.append(new EndToken(this.parseIndex, this.lineNumber));
		this.tokens.reset();
		return this.tokens;
	}

	// Parsing

	private void parseCharacter(int currentChar)
	{
		switch (currentChar)
		{
		case '`':
			this.parseBacktickIdentifier(currentChar);
			return;
		case '"':
			this.parseDoubleString(currentChar, false);
			return;
		case '\'':
			this.parseSingleString(currentChar);
			return;
		case '@':
			if (this.nextCodePoint() == '"')
			{
				this.parseVerbatimString(currentChar);
				return;
			}

			this.parseIdentifier('@', MOD_SYMBOL);
			return;
		case '/':
			int n = this.nextCodePoint();
			if (n == '*')
			{
				this.parseBlockComment(currentChar);
				return;
			}
			if (n == '/')
			{
				this.parseLineComment(currentChar);
				return;
			}
			this.parseIdentifier('/', MOD_SYMBOL);
			return;
		case '(':
			if (this.stringParens > 0)
			{
				this.stringParens++;
			}
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_PARENTHESIS, this.lineNumber, this.parseIndex++));
			return;
		case ')':
			if (this.stringParens > 0 && --this.stringParens == 0)
			{
				this.parseDoubleString(')', true);
				return;
			}

			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_PARENTHESIS, this.lineNumber, this.parseIndex++));
			return;
		case '[':
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_SQUARE_BRACKET, this.lineNumber, this.parseIndex++));
			return;
		case ']':
			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_SQUARE_BRACKET, this.lineNumber, this.parseIndex++));
			return;
		case '{':
			this.tokens.append(new SymbolToken(INSTANCE, OPEN_CURLY_BRACKET, this.lineNumber, this.parseIndex++));
			return;
		case '}':
			this.tokens.append(new SymbolToken(INSTANCE, CLOSE_CURLY_BRACKET, this.lineNumber, this.parseIndex++));
			return;
		case '.':
			n = this.nextCodePoint();
			if (LexerUtil.isIdentifierSymbol(n) || n == '.')
			{
				this.parseIdentifier('.', MOD_DOT);
				return;
			}
			this.tokens.append(new SymbolToken(INSTANCE, DOT, this.lineNumber, this.parseIndex++));
			return;
		case ';':
			this.tokens.append(new SymbolToken(INSTANCE, SEMICOLON, this.lineNumber, this.parseIndex++));
			return;
		case ',':
			this.tokens.append(new SymbolToken(INSTANCE, COMMA, this.lineNumber, this.parseIndex++));
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
			this.lineNumber++;
			// Fallthrough
		case ' ':
		case '\t':
			this.parseIndex++;
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

	private void parseBacktickIdentifier(int currentChar)
	{
		assert currentChar == '`';

		int startIndex = this.parseIndex++;
		int startLine = this.lineNumber;

		this.clearBuffer();

		while (true)
		{
			currentChar = this.codePoint();
			switch (currentChar)
			{
			case '\n':
				this.lineNumber++;
			case '\t':
			case '\b':
				continue;
			case EOF:
				this.error("identifier.backtick.unclosed");
				// Fallthrough
			case '`':
				this.parseIndex++;
				this.tokens.append(
					new IdentifierToken(Name.getSpecial(this.buffer.toString()), SPECIAL_IDENTIFIER, startLine,
					                    startIndex, this.parseIndex));
				return;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseSingleString(int currentChar)
	{
		assert currentChar == '\'';

		int startIndex = this.parseIndex++;
		int startLine = this.lineNumber;

		this.clearBuffer();

		while (true)
		{
			currentChar = this.codePoint();
			switch (currentChar)
			{
			case '\\':
				this.parseEscape(this.nextCodePoint());
				continue;
			case '\t':
			case '\b':
				continue;
			case '\n':
				this.lineNumber++;
				this.error("string.single.newline");
				continue;
			case EOF:
				this.error("string.single.unclosed");
				// Fallthrough
			case '\'':
				this.parseIndex++;
				this.tokens.append(new StringToken(this.buffer.toString(), SINGLE_QUOTED_STRING, startLine, startIndex,
				                                   this.parseIndex + 1));
				return;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseDoubleString(int currentChar, boolean stringPart)
	{
		assert currentChar == (stringPart ? ')' : '"');

		int startIndex = this.parseIndex++;
		int startLine = this.lineNumber;

		this.clearBuffer();

		while (true)
		{
			currentChar = this.codePoint();

			switch (currentChar)
			{
			case '\\':
				final int nextChar = this.nextCodePoint();
				if (nextChar == '(')
				{
					this.parseIndex += 2;
					if (this.stringParens > 0)
					{
						this.error("string.double.interpolation.nested");
						continue; // parse the rest of the string as normal
					}

					this.tokens.append(
						new StringToken(this.buffer.toString(), stringPart ? STRING_PART : STRING_START, startLine,
						                startIndex, this.parseIndex + 1));
					this.stringParens = 1;
					return;
				}

				this.parseEscape(nextChar);
				continue;
			case EOF:
				this.error("string.double.unclosed");
				// Fallthrough
			case '"':
				this.parseIndex++;
				this.tokens.append(
					new StringToken(this.buffer.toString(), stringPart ? STRING_END : STRING, startLine, startIndex,
					                this.parseIndex));
				return;
			case '\n':
				this.lineNumber++;
				break;
			case '\t':
				this.parseIndex++;
				continue;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseVerbatimString(int currentChar)
	{
		assert currentChar == '@';

		int startIndex = this.parseIndex;
		int startLine = this.lineNumber;

		assert this.nextCodePoint() == '"';
		this.parseIndex += 2;

		this.clearBuffer();

		while (this.hasNextCodePoint())
		{
			currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				this.error("string.verbatim.unclosed");
				// Fallthrough
			case '"':
				this.parseIndex++;
				this.tokens.append(
					new StringToken(this.buffer.toString(), VERBATIM_STRING, startLine, startIndex, this.parseIndex));
				return;
			case '\n':
				this.lineNumber++;
				this.parseIndex++;
				continue;
			case '\t':
				this.parseIndex++;
				continue;
			}

			this.buffer.appendCodePoint(currentChar);
			this.advance(currentChar);
		}
	}

	private void parseNumberLiteral(int currentChar)
	{
		final int radix;

		this.clearBuffer();
		if (currentChar == '0')
		{
			switch (this.nextCodePoint())
			{
			case 'o':
			case 'O':
				this.parseIndex++;
				radix = 8;
				break;
			case 'x':
			case 'X':
				this.parseIndex++;
				radix = 16;
				break;
			case 'b':
			case 'B':
				this.parseIndex++;
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

		int startIndex = this.parseIndex++;
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
				this.parseIndex++;
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
					this.parseIndex++;
					continue;
				}
				break;
			case '8':
			case '9':
				if (radix >= 10)
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					continue;
				}
				break;
			case 'a':
			case 'b':
			case 'c':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					continue;
				}
				break;
			case '_':
				this.parseIndex++;
				continue;
			case 'd':
			case 'D':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					continue;
				}
				// Fallthrough
				if (radix >= 10 && !LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.parseIndex++;
					type = 3; // double
					continue;
				}
				break;
			case '.':
				if (radix == 10 && LexerUtil.isDigit(this.nextCodePoint()))
				{
					this.buffer.append('.');
					this.parseIndex++;
					type = 3; // double
					continue;
				}
				break;
			case 'e':
			case 'E':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					continue;
				}
				if (radix == 10)
				{
					int n = this.nextCodePoint();
					if (LexerUtil.isDigit(n))
					{
						this.buffer.append((char) currentChar);
						this.parseIndex++;
						type = 3; // double
						continue;
					}
					if (n == '-')
					{
						this.buffer.append('e').append('-');
						this.parseIndex += 2;
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
					this.parseIndex++;
					continue;
				}
				if (radix >= 10 && !LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.parseIndex++;
					type = 2; // float
					continue;
				}
				break;
			case 'l':
			case 'L':
				if (!LexerUtil.isIdentifierPart(this.nextCodePoint()))
				{
					this.parseIndex++;
					type = 1; // long
					continue;
				}
				break;
			case 'p':
			case 'P':
				if (radix == 16)
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					type = 3; // float
					continue;
				}
				break;
			}

			switch (type)
			{
			case 0: // int
			{
				final IntToken token = new IntToken(0, this.lineNumber, startIndex, this.parseIndex);
				try
				{
					token.setValue(Integer.parseInt(this.buffer.toString(), radix));
				}
				catch (NumberFormatException ex)
				{
					this.error(token, "literal.integer.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 1: // long
			{
				final LongToken token = new LongToken(0, this.lineNumber, startIndex, this.parseIndex);
				try
				{
					token.setValue(Long.parseLong(this.buffer.toString(), radix));
				}
				catch (NumberFormatException ex)
				{
					this.error(token, "literal.long.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 2: // float
			{
				final FloatToken token = new FloatToken(0, this.lineNumber, startIndex, this.parseIndex);
				try
				{
					token.setValue(Float.parseFloat(this.buffer.toString()));
				}
				catch (NumberFormatException ex)
				{
					this.error(token, "literal.float.invalid");
				}

				this.tokens.append(token);
				return;
			}
			case 3: // double
			{
				final DoubleToken token = new DoubleToken(0, this.lineNumber, startIndex, this.parseIndex + 1);
				try
				{
					if (radix == 16)
					{
						this.buffer.insert(0, "0x");
					}
					token.setValue(Double.parseDouble(this.buffer.toString()));
				}
				catch (NumberFormatException ex)
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

	private void parseLineComment(int currentChar)
	{
		assert currentChar == '/';
		assert this.nextCodePoint() == '/';
		this.parseIndex += 2;

		while (true)
		{
			currentChar = this.codePoint();

			if (currentChar == EOF)
			{
				return;
			}
			if (currentChar == '\n')
			{
				this.parseIndex++;
				this.lineNumber++;
				return;
			}

			this.advance(currentChar);
		}
	}

	private void parseBlockComment(int currentChar)
	{
		assert currentChar == '/';
		assert this.nextCodePoint() == '*';

		int level = 1;
		this.parseIndex += 2;

		while (true)
		{
			currentChar = this.codePoint();

			switch (currentChar)
			{
			case EOF:
				this.error("comment.block.unclosed");
				return;
			case '\n':
				this.lineNumber++;
				this.parseIndex++;
				continue;
			case '/':
				if (this.nextCodePoint() == '*')
				{
					level++;
					this.parseIndex += 2;
					continue;
				}
				this.parseIndex++;
				continue;
			case '*':
				if (this.nextCodePoint() == '/')
				{
					level--;
					this.parseIndex += 2;

					if (level == 0)
					{
						return;
					}
					continue;
				}

				this.parseIndex++;
				continue;
			}

			this.advance(currentChar);
		}
	}

	private void parseIdentifier(int currentChar, int subtype)
	{
		int startIndex = this.parseIndex;

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
					this.parseIndex++;
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
					this.tokens.append(new SymbolToken(this.symbols, keyword, this.lineNumber, startIndex));
					return;
				}
				this.tokens.append(
					new IdentifierToken(Name.get(id), Tokens.LETTER_IDENTIFIER, this.lineNumber, startIndex,
					                    this.parseIndex));
				return;
			}
			case MOD_DOT:
				if (currentChar == '.')
				{
					this.buffer.append('.');
					this.parseIndex++;
					continue;
				}
				// Fallthrough
			case MOD_SYMBOL:
				if (currentChar == '_' || currentChar == '$')
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
					subtype = MOD_LETTER | MOD_SYMBOL;
					continue;
				}
				if (LexerUtil.isIdentifierSymbol(currentChar))
				{
					this.buffer.appendCodePoint(currentChar);
					this.parseIndex++;
					subtype = MOD_SYMBOL;
					continue;
				}
				break;
			case MOD_LETTER | MOD_SYMBOL:
				if (currentChar == '_' || currentChar == '$')
				{
					this.buffer.append((char) currentChar);
					this.parseIndex++;
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
				this.tokens.append(new SymbolToken(this.symbols, symbol, this.lineNumber, startIndex));
				return;
			}
			this.tokens.append(new IdentifierToken(Name.get(id), Tokens.SYMBOL_IDENTIFIER, this.lineNumber, startIndex,
			                                       this.parseIndex));
			return;
		}
	}

	private void parseEscape(int n)
	{
		switch (n)
		{
		case '"':
		case '\'':
		case '\\':
			this.buffer.append((char) n);
			this.parseIndex += 2;
			return;
		case 'n':
			this.buffer.append('\n');
			this.parseIndex += 2;
			return;
		case 't':
			this.buffer.append('\t');
			this.parseIndex += 2;
			return;
		case 'r':
			this.buffer.append('\r');
			this.parseIndex += 2;
			return;
		case 'b':
			this.buffer.append('\b');
			this.parseIndex += 2;
			return;
		case 'f':
			this.buffer.append('\f');
			this.parseIndex += 2;
			return;
		case 'u':
			int buf = 0;
			this.parseIndex += 2;

			if (this.codePoint() != '{')
			{
				this.error("escape.unicode.open_brace");
				return;
			}

			this.parseIndex++;
			loop:
			while (true)
			{
				int codePoint = this.codePoint();
				switch (codePoint)
				{
				case '\n':
					this.error("escape.unicode.newline");
					this.lineNumber++;
					// Fallthrough
				case ' ':
				case '_':
				case '\t':
					this.parseIndex++;
					continue;
				case '}':
					this.parseIndex++;
					break loop;
				}
				if (!LexerUtil.isHexDigit(codePoint))
				{
					this.error("escape.unicode.close_brace");
					this.advance(codePoint);
					break;
				}

				buf <<= 4;
				buf += Character.digit(codePoint, 16);
				this.parseIndex++;
			}

			this.buffer.appendCodePoint(buf);
			return;
		}

		this.error("escape.invalid");

		this.parseIndex += 2;
	}

	// Utility Methods

	private boolean hasNextCodePoint()
	{
		return this.parseIndex + 1 < this.length;
	}

	private int codePoint()
	{
		return this.parseIndex >= this.length ? 0 : this.code.codePointAt(this.parseIndex);
	}

	private int nextCodePoint()
	{
		return !this.hasNextCodePoint() ? 0 : this.code.codePointAt(this.parseIndex + 1);
	}

	private void advance(int currentChar)
	{
		this.parseIndex += Character.charCount(currentChar);
	}

	private void clearBuffer()
	{
		this.buffer.delete(0, this.buffer.length());
	}

	private void error(String key)
	{
		this.error(new CodePosition(this.lineNumber, this.parseIndex, this.parseIndex + 1), key);
	}

	private void error(ICodePosition position, String key)
	{
		this.markers.add(new SyntaxError(position, this.markers.getI18n().getString(key)));
	}
}
