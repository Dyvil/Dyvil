package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class TryParserManager extends ParserManager
{
	private boolean     hasSyntaxErrors;
	private boolean     reportErrors;
	
	public TryParserManager(IOperatorMap operatorMap)
	{
		super(operatorMap);
	}

	@Override
	public void report(Marker error)
	{
		if (!this.hasSyntaxErrors)
		{
			this.hasSyntaxErrors = error.isError();
		}

		if (this.reportErrors)
		{
			super.report(error);
		}
	}

	public boolean parse(MarkerList markers, TokenIterator tokens, Parser parser, boolean reportErrors)
	{
		tokens.reset();
		this.reset(markers, tokens);
		return this.parse(parser, reportErrors);
	}

	public boolean parse(Parser parser, boolean reportErrors)
	{
		this.parser = parser;
		this.hasSyntaxErrors = false;
		this.reportErrors = reportErrors;
		
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
				return false;
			}
			
			if (this.hasSyntaxErrors && !this.reportErrors)
			{
				return false;
			}
		}
		
		this.parseRemaining(token);
		
		return !this.hasSyntaxErrors || this.reportErrors;
	}
}
