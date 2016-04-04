package dyvil.tools.compiler.parser;

import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public class ParserManager implements IParserManager
{
	protected Parser parser;
	
	protected MarkerList markers;
	
	protected TokenIterator tokens;
	protected int           skip;
	protected boolean       reparse;
	protected boolean       hasStopped;
	
	public ParserManager()
	{
	}

	public ParserManager(TokenIterator tokens, MarkerList markers)
	{
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
	public void report(IToken token, String message)
	{
		this.report(new SyntaxError(token, this.markers.getI18n().getString(message)));
	}

	@Override
	public void report(Marker error)
	{
		this.markers.add(error);
	}

	public final void parse(Parser parser)
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

	protected void reportError(ICodePosition position, Throwable ex)
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
	public void jump(IToken token)
	{
		this.tokens.jump(token);
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
