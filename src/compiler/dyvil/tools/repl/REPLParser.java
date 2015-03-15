package dyvil.tools.repl;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.InferredSemicolon;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class REPLParser implements IParserManager
{
	private Parser	parser;
	private int		skip;
	private boolean	reparse;
	
	public boolean parse(TokenIterator tokens, Parser parser)
	{
		this.parser = parser;
		this.skip = 0;
		this.reparse = false;
		
		IToken token = null, prev = null;
		int index = 0;
		tokens.reset();
		while (tokens.hasNext())
		{
			token = tokens.next();
			token.setIndex(index);
			token.setPrev(prev);
			index++;
			prev = token;
		}
		
		int type = prev.type();
		if (!ParserUtil.isSeperator(type) && type != (Tokens.TYPE_IDENTIFIER | Tokens.MOD_SYMBOL))
		{
			IToken semicolon = new InferredSemicolon(prev.index() + 1, prev.endLine(), prev.endIndex());
			semicolon.setPrev(prev);
			prev.setNext(semicolon);
		}
		
		tokens.reset();
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
				StringBuilder buf = new StringBuilder();
				ex.log(DyvilREPL.currentCode, buf);
				System.out.println(buf.toString());
				return false;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			
			if (this.parser == null)
			{
				break;
			}
			
			if (DyvilCompiler.parseStack)
			{
				System.out.println(token + ":\t\t" + this.parser.getName() + " @ " + this.parser.getMode());
			}
		}
		
		return true;
	}
	
	protected boolean isRoot()
	{
		return this.parser == Parser.rootParser;
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
		parser.setParent(this.parser);
		this.parser = parser;
	}
	
	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.setParent(this.parser);
		this.parser = parser;
		this.reparse = reparse;
	}
	
	@Override
	public void popParser()
	{
		this.parser = this.parser.getParent();
	}
	
	@Override
	public void popParser(boolean reparse)
	{
		this.parser = this.parser.getParent();
		this.reparse = reparse;
	}
}
