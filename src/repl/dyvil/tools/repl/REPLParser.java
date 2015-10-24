package dyvil.tools.repl;

import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.tools.parsing.token.IToken;

public class REPLParser extends ParserManager
{
	private boolean syntaxErrors;
	
	@Override
	public void report(IToken token, String message)
	{
		this.syntaxErrors = true;
		if (this.markers != null)
		{
			this.markers.add(new SyntaxError(token, message));
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
				if (!token.isInferred())
				{
					this.report(token, "Unexpected Token: " + token);
				}
				continue;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			
			if (this.syntaxErrors && this.markers == null)
			{
				return false;
			}
		}
		
		return !this.syntaxErrors;
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		Operator op = DyvilREPL.context.getOperator(name);
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
