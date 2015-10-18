package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.parsing.token.InferredSemicolon;

public final class SemicolonInference
{
	private static final int EXCLUDED_PREV_TYPES = Tokens.KEYWORD | Tokens.SYMBOL;
	
	private static final int VALUE_KEYWORDS = DyvilKeywords.TRUE | DyvilKeywords.FALSE | DyvilKeywords.BREAK | DyvilKeywords.CONTINUE | DyvilKeywords.THIS
			| DyvilKeywords.SUPER | DyvilSymbols.ELLIPSIS | DyvilSymbols.WILDCARD;
			
	private SemicolonInference()
	{
		// no instances
	}
	
	public static void inferSemicolons(IToken first)
	{
		if (first == null)
		{
			return;
		}
		
		IToken prev = first;
		IToken next = first.next();
		while (next != null)
		{
			inferSemicolon(prev, next);
			prev = next;
			next = next.next();
		}
		
		prev = first;
		next = first.next();
		while (next != null)
		{
			next.setPrev(prev);
			prev = next;
			next = next.next();
		}
		
		prev.setNext(new InferredSemicolon(prev.endLine(), prev.endIndex() + 1));
	}
	
	private static void inferSemicolon(IToken prev, IToken next)
	{
		int prevLine = prev.endLine();
		int nextLine = next.startLine();
		if (nextLine == prevLine)
		{
			return;
		}
		
		if (nextLine == prevLine + 1)
		{
			// Check last token on line in question
			
			int prevType = prev.type();
			if ((prevType & EXCLUDED_PREV_TYPES) != 0 && (prevType & VALUE_KEYWORDS) == 0)
			{
				return;
			}
			
			switch (prevType)
			{
			case Tokens.STRING_START:
			case Tokens.STRING_PART:
			case BaseSymbols.OPEN_PARENTHESIS:
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			case BaseSymbols.OPEN_CURLY_BRACKET:
				return;
			}
			
			// Check first token on the next line
			
			int nextType = next.type();
			if ((nextType & Tokens.SYMBOL) != 0)
			{
				return;
			}
			
			switch (nextType)
			{
			case Tokens.STRING_PART:
			case Tokens.STRING_END:
			case BaseSymbols.OPEN_CURLY_BRACKET:
			case BaseSymbols.CLOSE_PARENTHESIS:
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				return;
			}
		}
		
		IToken semicolon = new InferredSemicolon(prevLine, prev.endIndex());
		semicolon.setNext(next);
		prev.setNext(semicolon);
	}
}
