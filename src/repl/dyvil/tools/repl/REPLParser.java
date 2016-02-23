package dyvil.tools.repl;

import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class REPLParser extends ParserManager
{
	private REPLContext context;
	private boolean     hasSyntaxErrors;
	private boolean     reportErrors;
	
	public REPLParser(REPLContext context)
	{
		this.context = context;
	}

	@Override
	public void report(Marker error)
	{
		this.hasSyntaxErrors = true;
		if (this.reportErrors)
		{
			super.report(error);
		}
	}

	public boolean parse(MarkerList markers, TokenIterator tokens, Parser parser, int errorTargetMode)
	{
		this.tokens = tokens;
		this.parser = parser;
		this.skip = 0;
		this.reparse = false;
		this.markers = markers;
		this.hasSyntaxErrors = false;
		this.reportErrors = errorTargetMode < 0;
		
		IToken token = null;
		
		tokens.reset();
		
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
				
				token = tokens.next();
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

				if (this.reportErrors || this.parser.getMode() > errorTargetMode)
				{
					this.reportErrors = true;
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace(this.context.repl.getErrorOutput());
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
	
	@Override
	public Operator getOperator(Name name)
	{
		Operator op = this.context.getOperator(name);
		if (op != null)
		{
			return op;
		}
		return Types.LANG_HEADER.getOperator(name);
	}
	
	protected boolean isRoot()
	{
		return this.parser == Parser.rootParser;
	}
}
