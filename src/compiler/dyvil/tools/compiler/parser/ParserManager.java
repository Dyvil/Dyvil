package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public class ParserManager implements IParserManager
{
	protected Parser parser;
	
	protected MarkerList markers;
	
	protected IOperatorMap operators;
	
	protected TokenIterator	tokens;
	protected int			skip;
	protected boolean		reparse;
	
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
		this.markers.add(new SyntaxError(token, message));
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
		
		while (true)
		{
			if (this.reparse)
			{
				this.reparse = false;
			}
			else
			{
				token = tokens.next();
				
				if (token == null)
				{
					break;
				}
			}
			
			if (this.skip > 0)
			{
				this.skip--;
				continue;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (Exception ex)
			{
				DyvilCompiler.error("ParserManager", "parseToken", ex);
				this.markers.add(new SyntaxError(token, "Failed to parse token '" + token + "': " + ex.getMessage()));
			}
			
			if (this.parser == null)
			{
				break;
			}
			
			if (DyvilCompiler.parseStack)
			{
				System.out.println(token + ":\t\t" + this.parser.name + " @ " + this.parser.mode);
			}
		}
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
