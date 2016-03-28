package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class TryParserManager extends ParserManager
{
	private boolean hasSyntaxErrors;
	private boolean reportErrors;

	public static final int REPORT_ERRORS = 1;
	public static final int EXIT_ON_ROOT  = 2;

	public TryParserManager()
	{
		super();
	}

	public TryParserManager(TokenIterator tokens, MarkerList markers)
	{
		super(tokens, markers);
	}

	@Override
	public void report(Marker error)
	{
		final boolean isError = error.isError();
		if (!this.hasSyntaxErrors)
		{
			this.hasSyntaxErrors = isError;
		}

		if (this.reportErrors || !isError)
		{
			super.report(error);
		}
	}

	public boolean parse(Parser parser, boolean reportErrors)
	{
		return this.parse(parser, reportErrors ? REPORT_ERRORS : 0);
	}

	public boolean parse(Parser parser, int flags)
	{
		this.parser = parser;
		this.hasSyntaxErrors = false;
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
				if (!this.tokens.hasNext())
				{
					break;
				}

				token = this.tokens.next();
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
					return this.success();
				}
				if (token != null && !token.isInferred())
				{
					this.report(Markers.syntaxError(token, "parser.unexpected", token));
				}
				continue;
			}

			try
			{
				this.parser.parse(this, token);

				if (!this.reportErrors && this.parser.reportErrors())
				{
					if (this.hasSyntaxErrors)
					{
						return false;
					}

					this.reportErrors = true;
				}
			}
			catch (Exception ex)
			{
				this.report(Markers.parserError(token, ex));
				return this.success();
			}

			if (!this.success())
			{
				return false;
			}
		}

		this.parseRemaining(token);

		return this.success();
	}

	private boolean success()
	{
		return !this.hasSyntaxErrors || this.reportErrors;
	}
}
