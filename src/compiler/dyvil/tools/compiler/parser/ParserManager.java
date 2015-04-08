package dyvil.tools.compiler.parser;

import java.util.Map;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public final class ParserManager implements IParserManager
{
	protected Parser			parser;
	
	public Map<Name, Operator>	operators;
	
	protected TokenIterator		tokens;
	protected int				skip;
	protected boolean			reparse;
	
	public ParserManager()
	{
	}
	
	/**
	 * Creates a new {@link ParserManager} with the given {@link Parser}
	 * {@code parser} as the current parser, and calls the parser's
	 * {@link Parser#begin(ParserManager) begin} method.
	 * 
	 * @see Parser#begin(ParserManager)
	 * @param parser
	 *            the parser
	 */
	public ParserManager(Parser parser)
	{
		this.parser = parser;
	}
	
	public final void parse(MarkerList markers, TokenIterator tokens)
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
			catch (SyntaxError ex)
			{
				// if (this.jumpBackToken != null)
				// {
				// tokens.jump(this.jumpBackToken);
				// this.popParser();
				// this.jumpBackToken = null;
				// }
				// else
				{
					if (ex.reparse)
					{
						this.reparse = true;
					}
					markers.add(ex);
				}
			}
			catch (Exception ex)
			{
				// if (this.jumpBackToken != null)
				// {
				// tokens.jump(this.jumpBackToken);
				// this.popParser();
				// this.jumpBackToken = null;
				// }
				// else
				{
					DyvilCompiler.logger.throwing("ParserManager", "parseToken", ex);
					markers.add(new SyntaxError(token, "Failed to parse token '" + token + "': " + ex.getMessage()));
				}
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
	public Operator getOperator(Name name)
	{
		Operator op = this.operators.get(name);
		if (op != null)
		{
			return op;
		}
		return Operators.map.get(name);
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
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		// this.jumpBackToken = null;
		this.parser = this.parser.parent;
	}
	
	@Override
	public void popParser(boolean reparse)
	{
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		// this.jumpBackToken = null;
		this.parser = this.parser.parent;
		this.reparse = reparse;
	}
}
