package dyvil.tools.parsing;

import dyvil.lang.Name;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.lexer.Symbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.source.position.SourcePosition;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.parsing.token.IdentifierToken;
import dyvil.tools.parsing.token.SymbolToken;

public class ParserManager implements IParserManager
{
	protected Parser parser;

	protected MarkerList markers;

	protected Symbols       symbols;
	protected TokenIterator tokens;
	protected int           skip;
	protected boolean       reparse;
	protected boolean       hasStopped;

	public ParserManager(Symbols symbols)
	{
		this.symbols = symbols;
	}

	public ParserManager(Symbols symbols, TokenIterator tokens, MarkerList markers)
	{
		this.symbols = symbols;
		this.tokens = tokens;
		this.markers = markers;
	}

	@Override
	public MarkerList getMarkers()
	{
		return this.markers;
	}

	@Override
	public TokenIterator getTokens()
	{
		return this.tokens;
	}

	public void reset()
	{
		this.skip = 0;
		this.reparse = false;
		this.hasStopped = false;
	}

	public void reset(MarkerList markers, TokenIterator tokens)
	{
		this.reset();
		this.markers = markers;
		this.tokens = tokens;
	}

	@Override
	public IToken split(IToken token, int length)
	{
		final String stringValue = token.stringValue();
		if (length == stringValue.length())
		{
			// the second part would be empty, so it stays a single token
			return token;
		}

		final int line = token.startLine();
		final int startIndex = token.startColumn();

		final IToken prev = token.prev();
		final IToken token1 = this.toToken(stringValue.substring(0, length), startIndex, line);
		final IToken token2 = this.toToken(stringValue.substring(length), startIndex + length, line);
		final IToken next = token.next();

		// Re-link the tokens
		prev.setNext(token1);
		token1.setPrev(prev);
		token1.setNext(token2);
		token2.setPrev(token1);
		token2.setNext(next);
		next.setPrev(token2);

		return token1;
	}

	private IToken toToken(String identifier, int start, int line)
	{
		final int length = identifier.length();
		final int lastCodePoint = identifier.codePointBefore(length);

		if (LexerUtil.isIdentifierSymbol(lastCodePoint) || LexerUtil.isIdentifierConnector(lastCodePoint))
		{
			final int symbol = this.symbols.getSymbolType(identifier);
			if (symbol != 0)
			{
				return new SymbolToken(this.symbols, symbol, line, start);
			}
			return new IdentifierToken(Name.from(identifier), Tokens.SYMBOL_IDENTIFIER, line, start, start + length);
		}

		final int keyword = this.symbols.getKeywordType(identifier);
		if (keyword != 0)
		{
			return new SymbolToken(this.symbols, keyword, line, start);
		}

		return new IdentifierToken(dyvil.lang.Name.from(identifier), Tokens.LETTER_IDENTIFIER, line, start, start + length);
	}

	@Override
	public void splitJump(IToken token, int length)
	{
		this.setNext(this.split(token, length).next());
	}

	@Override
	public void splitReparse(IToken token, int length)
	{
		this.setNext(this.split(token, length));
	}

	@Override
	public void report(SourcePosition position, String message)
	{
		this.report(new SyntaxError(position, this.markers.getI18n().getString(message)));
	}

	@Override
	public void report(Marker error)
	{
		this.markers.add(error);
	}

	public void parse(Parser parser)
	{
		this.parser = parser;

		IToken token = null;

		while (!this.hasStopped)
		{
			if (this.reparse)
			{
				this.reparse = false;
			}
			else
			{
				token = this.tokens.next();
				if (token.type() == Tokens.EOF)
				{
					break;
				}
			}

			if (this.skip > 0)
			{
				this.skip--;
				continue;
			}

			if (this.parser == null)
			{
				this.reportUnparsed(token);
				continue;
			}

			this.tryParse(token, this.parser);
		}

		this.parseRemaining(token);
	}

	protected void reportUnparsed(IToken token)
	{
		if (token != null && !token.isInferred())
		{
			this.report(new SyntaxError(token, this.markers.getI18n().getString("parser.unexpected", token)));
		}
	}

	protected void parseRemaining(IToken token)
	{
		if (token == null || this.hasStopped)
		{
			return;
		}

		while (this.parser != null)
		{
			token = token.next();

			Parser prevParser = this.parser;
			int mode = prevParser.getMode();

			this.tryParse(token, prevParser);

			if (this.parser == prevParser && this.parser.getMode() == mode)
			{
				break;
			}
		}
	}

	protected void tryParse(IToken token, Parser prevParser)
	{
		try
		{
			prevParser.parse(this, token);
		}
		catch (Exception ex)
		{
			this.reportError(token, ex);
		}
	}

	protected void reportError(SourcePosition position, Throwable ex)
	{
		final Marker marker = new SyntaxError(position, this.markers.getI18n()
		                                                            .getString("parser.error", position.toString(),
		                                                                       ex.getLocalizedMessage()));
		marker.addError(ex);
		this.markers.add(marker);
	}

	@Override
	public void stop()
	{
		this.hasStopped = true;
	}

	@Override
	public void skip()
	{
		this.skip++;
	}

	@Override
	public void skip(int tokens)
	{
		this.skip += tokens;
	}

	@Override
	public void reparse()
	{
		this.reparse = true;
	}

	@Override
	public void setNext(IToken token)
	{
		this.tokens.setNext(token);
		this.reparse = false;
	}

	@Override
	public void setParser(Parser parser)
	{
		this.parser = parser;
	}

	@Override
	public Parser getParser()
	{
		return this.parser;
	}

	@Override
	public void pushParser(Parser parser)
	{
		parser.parent = this.parser;
		this.parser = parser;
	}

	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.parent = this.parser;
		this.parser = parser;
		this.reparse = reparse;
	}

	@Override
	public void popParser()
	{
		this.parser = this.parser.parent;
	}

	@Override
	public void popParser(boolean reparse)
	{
		this.parser = this.parser.parent;
		this.reparse = reparse;
	}
}
