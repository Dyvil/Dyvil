package dyvilx.tools.parsing.lexer;

import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.token.EndToken;

public abstract class Lexer
{
	protected final MarkerList markers;
	protected final Symbols    symbols;

	protected String    code;
	protected int       length;
	protected TokenList tokens;

	protected StringBuilder buffer = new StringBuilder();

	protected int cursor;
	protected int column;
	protected int line;

	public Lexer(MarkerList markers, Symbols symbols)
	{
		this.markers = markers;
		this.symbols = symbols;
	}

	public int getCursor()
	{
		return this.cursor;
	}

	public int getColumn()
	{
		return this.column;
	}

	public int getLine()
	{
		return this.line;
	}

	public TokenList tokenize(String code)
	{
		return this.tokenize(code, 0, 1, 0);
	}

	@SuppressWarnings("SameParameterValue")
	public TokenList tokenize(String code, int cursor, int line, int column)
	{
		this.init(code, cursor, line, column);

		while (true)
		{
			final int currentChar = this.codePoint();
			if (currentChar == 0)
			{
				break;
			}

			this.parseCharacter(currentChar);
		}

		this.finish();
		return this.tokens;
	}

	protected void init(String code, int cursor, int line, int column)
	{
		this.tokens = new TokenList();
		this.code = code;
		this.length = code.length();
		this.cursor = cursor;
		this.line = line;
		this.column = column;
		this.clearBuffer();
	}

	protected void finish()
	{
		this.tokens.append(new EndToken(this.cursor, this.line));
	}

	protected abstract void parseCharacter(int c);

	protected void useSubLexer(Lexer sublexer)
	{
		final TokenList tokens = sublexer.tokenize(this.code, this.cursor, this.line, this.column);
		this.tokens.addAll(tokens);

		this.cursor = sublexer.getCursor();
		this.line = sublexer.getLine();
		this.column = sublexer.getColumn();
	}

	// Utility Methods

	protected int codePoint()
	{
		return this.cursor >= this.length ? 0 : this.code.codePointAt(this.cursor);
	}

	protected int nextCodePoint()
	{
		return this.cursor + 1 >= this.length ? 0 : this.code.codePointAt(this.cursor + 1);
	}

	protected void advance()
	{
		this.cursor++;
		this.column++;
	}

	protected void advance2()
	{
		this.cursor += 2;
		this.column += 2;
	}

	protected void advance(int currentChar)
	{
		this.cursor += Character.charCount(currentChar);
		this.column++;
	}

	protected void newLine()
	{
		this.line++;
		this.column = 0;
		this.cursor++;
	}

	protected void clearBuffer()
	{
		this.buffer.delete(0, this.buffer.length());
	}

	protected void error(String key)
	{
		this.error(SourcePosition.apply(this.line, this.column, this.column + 1), key);
	}

	protected void error(SourcePosition position, String key)
	{
		this.markers.add(new Marker(position, MarkerLevel.SYNTAX, this.markers.getI18n().getString(key)));
	}
}
