package dyvil.tools.repl;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.InferredSemicolon;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public class REPLParser extends ParserManager
{
	private boolean		syntaxErrors;
	
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
		
		super.parse(tokens);
		
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
