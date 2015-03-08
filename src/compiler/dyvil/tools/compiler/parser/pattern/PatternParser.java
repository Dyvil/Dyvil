package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.pattern.*;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.Tokens;

public class PatternParser extends Parser implements IValued
{
	public static final int	PATTERN	= 1;
	public static final int	IF		= 2;
	
	protected ICase	patterned;
	
	public PatternParser(ICase patterned)
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
			IPattern p = parsePrimitive(token, type);
			if (p != null)
			{
				pm.popParser();
				
				this.patterned.setPattern(p);
				return;
			}
			
			throw new SyntaxError(token, "Invalid Pattern");
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

	@Override
	public void setValue(IValue value)
	{
	}

	@Override
	public IValue getValue()
	{
		return null;
	}
}
