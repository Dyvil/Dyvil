package dyvil.tools.repl;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.parsing.token.InferredSemicolon;

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
		
		IToken token = null, prev = null;
		tokens.reset();
		while (tokens.hasNext())
		{
			token = tokens.next();
			token.setPrev(prev);
			prev = token;
		}
		
		if (prev == null)
		{
			return false;
		}
		
		int type = prev.type();
		if (!ParserUtil.isSeperator(type) && type != (Tokens.IDENTIFIER | Tokens.MOD_SYMBOL))
		{
			IToken semicolon = new InferredSemicolon(prev.endLine(), prev.endIndex());
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
				break;
			}
			
			if (DyvilCompiler.parseStack)
			{
				System.out.println(token + ":\t\t" + this.parser.getName() + " @ " + this.parser.getMode());
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
