package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.pattern.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class PatternParser extends Parser
{
	public static final int	PATTERN		= 1;
	public static final int	ARRAY_END	= 2;
	public static final int	TUPLE_END	= 4;
	
	protected IPatterned	patterned;
	
	private IPattern pattern;
	
	public PatternParser(IPatterned patterned)
	{
		this.patterned = patterned;
		this.mode = PATTERN;
	}
	
	@Override
	public void reset()
	{
		this.mode = PATTERN;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0 || type == Tokens.COLON)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == PATTERN)
		{
			if (ParserUtil.isIdentifier(type))
			{
				int nextType = token.next().type();
				if (nextType == Tokens.EQUALS)
				{
					BindingPattern bp = new BindingPattern(token.raw(), token.value());
					this.patterned.setPattern(bp);
					this.patterned = bp;
					pm.skip();
					return;
				}
				// TODO Case Class Patterns
			}
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				this.mode = ARRAY_END;
				return;
			}
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				TuplePattern tp = new TuplePattern(token);
				this.pattern = tp;
				this.patterned.setPattern(tp);
				this.mode = TUPLE_END;
				pm.pushParser(new PatternListParser(tp));
				return;
			}
			
			IPattern p = parsePrimitive(token, type);
			if (p != null)
			{
				pm.popParser();
				
				this.patterned.setPattern(p);
				return;
			}
			
			throw new SyntaxError(token, "Invalid Pattern");
		}
		if (this.mode == ARRAY_END)
		{
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				this.pattern.expandPosition(token);
				pm.popParser();
				return;
			}
			this.pattern.expandPosition(token.prev());
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Array Pattern - '}' expected");
		}
		if (this.mode == TUPLE_END)
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.pattern.expandPosition(token);
				pm.popParser();
				return;
			}
			this.pattern.expandPosition(token.prev());
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Tuple Pattern - ')' expected");
		}
	}
	
	public static IPattern parsePrimitive(IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.TRUE:
			return new BooleanPattern(token.raw(), true);
		case Tokens.FALSE:
			return new BooleanPattern(token.raw(), false);
		case Tokens.WILDCARD:
			return new WildcardPattern(token.raw());
		case Tokens.NULL:
			return new NullPattern(token.raw());
		case Tokens.TYPE_STRING:
			return new StringPattern(token.raw(), (String) token.object());
		case Tokens.TYPE_CHAR:
			return new CharPattern(token.raw(), (Character) token.object());
		case Tokens.TYPE_INT:
			return new IntPattern(token.raw(), (Integer) token.object());
		case Tokens.TYPE_LONG:
			return new LongPattern(token.raw(), (Long) token.object());
		case Tokens.TYPE_FLOAT:
			return new FloatPattern(token.raw(), (Float) token.object());
		case Tokens.TYPE_DOUBLE:
			return new DoublePattern(token.raw(), (Double) token.object());
		}
		return null;
	}
}
