package dyvil.tools.compiler.parser.dwt;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.dwt.DWTList;
import dyvil.tools.compiler.ast.dwt.DWTNode;
import dyvil.tools.compiler.ast.dwt.DWTReference;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class DWTValueParser extends Parser
{
	public static final int	VALUE		= 1;
	public static final int	LIST_END	= 2;
	
	public IValued			valued;
	
	public DWTValueParser(IValued valued)
	{
		this.valued = valued;
		this.mode = VALUE;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == VALUE)
		{
			if (ParserUtil.isIdentifier(type))
			{
				if (token.next().isType(Tokens.OPEN_CURLY_BRACKET))
				{
					DWTNode node = new DWTNode(token.raw(), token.value());
					this.valued.setValue(node);
					pm.popParser();
					pm.pushParser(new DWTParser(node), true);
					return true;
				}
				
				this.valued.setValue(new DWTReference(token.raw(), token.value()));
				pm.popParser();
				return true;
			}
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				ValueList list = new DWTList(token);
				this.mode = LIST_END;
				this.valued.setValue(list);
				pm.pushParser(new DWTListParser(list));
				return true;
			}
			
			IValue primitive = parsePrimitive(token, type);
			if (primitive != null)
			{
				this.valued.setValue(primitive);
				pm.popParser();
				return true;
			}
			
			throw new SyntaxError(token, "Invalid Property Value");
		}
		if (this.mode == LIST_END)
		{
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				return true;
			}
		}
		return false;
	}
	
	public static IValue parsePrimitive(IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.TRUE:
			return new BooleanValue(token.raw(), true);
		case Tokens.FALSE:
			return new BooleanValue(token.raw(), false);
		case Tokens.TYPE_STRING:
			return new StringValue(token.raw(), (String) token.object());
		case Tokens.TYPE_CHAR:
			return new CharValue(token.raw(), (Character) token.object());
		case Tokens.TYPE_INT:
			return new IntValue(token.raw(), (Integer) token.object());
		case Tokens.TYPE_LONG:
			return new LongValue(token.raw(), (Long) token.object());
		case Tokens.TYPE_FLOAT:
			return new FloatValue(token.raw(), (Float) token.object());
		case Tokens.TYPE_DOUBLE:
			return new DoubleValue(token.raw(), (Double) token.object());
		}
		return null;
	}
}
