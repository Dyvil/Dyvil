package dyvil.tools.repl;

import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class REPLParser extends ParserManager
{
	private REPLContext context;
	private boolean     syntaxErrors;
	
	public REPLParser(REPLContext context)
	{
		this.context = context;
	}

	@Override
	public void report(Marker error)
	{
		this.syntaxErrors = true;
		if (this.markers != null)
		{
			super.report(error);
		}
	}

	public boolean parse(MarkerList markers, TokenIterator tokens, Parser parser)
	{
		this.tokens = tokens;
		this.parser = parser;
		this.skip = 0;
		this.reparse = false;
		this.markers = markers;
		this.syntaxErrors = false;
		
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
					this.report(MarkerMessages.createSyntaxError(token, "parser.unexpected", token));
				}
				continue;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (Exception ex)
			{
				ex.printStackTrace(this.context.repl.getErrorOutput());
				return false;
			}
			
			if (this.syntaxErrors && this.markers == null)
			{
				return false;
			}
		}
		
		this.parseRemaining(token);
		
		return !this.syntaxErrors;
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
