package dyvilx.tools.parsing;

import dyvilx.tools.parsing.lexer.Symbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.token.IToken;

import java.util.ArrayList;
import java.util.List;

public class TryParserManager extends ParserManager
{
	private boolean hasSyntaxErrors;
	private boolean reportErrors;

	private List<IToken> splitTokens;

	public static final int REPORT_ERRORS = 1;
	public static final int EXIT_ON_ROOT  = 2;

	public TryParserManager(Symbols symbols)
	{
		super(symbols);
	}

	public TryParserManager(Symbols symbols, TokenIterator tokens)
	{
		super(symbols, tokens, null);
	}

	@Deprecated
	public TryParserManager(Symbols symbols, TokenIterator tokens, MarkerList markers)
	{
		super(symbols, tokens, markers);
	}

	@Override
	public void report(Marker error)
	{
		final boolean isError = error.isError();
		if (!this.hasSyntaxErrors && isError)
		{
			this.hasSyntaxErrors = true;
		}

		if (this.reportErrors || !isError)
		{
			super.report(error);
		}
	}

	private void setNextAndReset(IToken token)
	{
		this.reset();
		this.tokens.setNext(token);

		if (this.splitTokens == null || this.splitTokens.isEmpty())
		{
			return;
		}

		// Restore all tokens that have been split
		for (IToken splitToken : this.splitTokens)
		{
			// The original tokens prev and next fields have not been updated by the split method

			splitToken.prev().setNext(splitToken);
			splitToken.next().setPrev(splitToken);
		}

		this.splitTokens.clear();
	}

	@Override
	public IToken split(IToken token, int length)
	{
		final IToken split = super.split(token, length);
		if (split == token)
		{
			return token;
		}

		if (this.splitTokens == null)
		{
			this.splitTokens = new ArrayList<>();
		}
		this.splitTokens.add(token);

		return split;
	}

	public boolean tryParse(IParserManager pm, Parser parser, IToken token, int flags)
	{
		final TokenIterator tokens = pm.getTokens();
		final MarkerList markers = pm.getMarkers();

		this.reset(markers, tokens);

		// Have to rewind one token because the TryParserManager assumes the TokenIterator is at the beginning
		// (i.e. no tokens have been returned by next() yet)
		tokens.setNext(token);

		if (!this.parse(parser, markers, flags))
		{
			// Reset to the next token and restore split tokens
			this.setNextAndReset(token);
			return false;
		}

		this.reset();

		tokens.setNext(tokens.lastReturned());
		return true;
	}

	@Override
	@Deprecated
	public void parse(Parser parser)
	{
		this.parse(parser, this.markers, 0);
	}

	public boolean parse(Parser parser, MarkerList markers, int flags)
	{
		this.parser = parser;
		this.hasSyntaxErrors = false;
		this.markers = new MarkerList(markers.getI18n());
		this.reportErrors = (flags & REPORT_ERRORS) != 0;

		IToken token = null;

		while (true)
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
				if ((flags & EXIT_ON_ROOT) != 0)
				{
					return this.success(markers);
				}
				this.reportUnparsed(token);
				continue;
			}
			if (!this.reportErrors && this.parser.reportErrors())
			{
				if (this.hasSyntaxErrors)
				{
					return this.success(markers);
				}

				this.reportErrors = true;
			}

			try
			{
				this.parser.parse(this, token);
			}
			catch (Exception ex)
			{
				this.reportError(token, ex);
				return this.success(markers);
			}

			if (this.hasSyntaxErrors && !this.reportErrors)
			{
				return this.success(markers);
			}
		}

		this.parseRemaining(token);
		this.reparse = false;

		return this.success(markers);
	}

	private boolean success(MarkerList markers)
	{
		if (!this.hasSyntaxErrors || this.reportErrors)
		{
			markers.addAll(this.markers);
			return true;
		}
		return false;
	}
}
