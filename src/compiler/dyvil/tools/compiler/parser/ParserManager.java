package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class ParserManager implements IParserManager
{
	protected Parser parser;
	
	protected MarkerList markers;
	
	protected IOperatorMap operators;
	
	protected TokenIterator tokens;
	protected int           skip;
	protected boolean       reparse;
	protected boolean       hasStopped;
	
	public ParserManager()
	{
	}
	
	public ParserManager(Parser parser, MarkerList markers, IOperatorMap operators)
	{
		this.parser = parser;
		this.markers = markers;
		this.operators = operators;
	}
	
	@Override
	public void report(IToken token, String message)
	{
		this.report(Markers.syntaxError(token, message));
	}

	@Override
	public void report(Marker error)
	{
		this.markers.add(error);
	}

	@Override
	public void setOperatorMap(IOperatorMap operators)
	{
		this.operators = operators;
	}
	
	@Override
	public IOperatorMap getOperatorMap()
	{
		return this.operators;
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		Operator op = this.operators.getOperator(name);
		if (op != null)
		{
			return op;
		}
		return Types.LANG_HEADER.getOperator(name);
	}
	
	public final void parse(TokenIterator tokens)
	{
		this.tokens = tokens;
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
			}
			catch (Exception ex)
			{
				this.report(Markers.parserError(token, ex));
			}
		}
		
		this.parseRemaining(token);
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
			
			prevParser.parse(this, token);
			
			if (this.parser == prevParser && this.parser.getMode() == mode)
			{
				break;
			}
		}
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
