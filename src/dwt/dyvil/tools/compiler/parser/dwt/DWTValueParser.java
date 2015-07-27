package dyvil.tools.compiler.parser.dwt;

import dyvil.tools.compiler.ast.dwt.DWTNode;
import dyvil.tools.compiler.ast.dwt.DWTReference;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class DWTValueParser extends Parser
{
	public static final int	VALUE		= 1;
	public static final int	LIST_END	= 2;
	
	protected IValued valued;
	
	public DWTValueParser(IValued valued)
	{
		this.valued = valued;
		this.mode = VALUE;
	}
	
	@Override
	public void reset()
	{
		this.mode = VALUE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == VALUE)
		{
			if (ParserUtil.isIdentifier(type))
			{
				if (token.next().type() == Symbols.OPEN_CURLY_BRACKET)
				{
					DWTNode node = new DWTNode(token.raw(), token.nameValue());
					this.valued.setValue(node);
					pm.popParser();
					pm.pushParser(new DWTParser(node), true);
					return;
				}
				
				this.valued.setValue(new DWTReference(token.raw(), token.nameValue()));
				pm.popParser();
				return;
			}
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				Array list = new Array(token);
				this.mode = LIST_END;
				this.valued.setValue(list);
				pm.pushParser(new ExpressionListParser(list));
				return;
			}
			
			IValue primitive = ParserUtil.parsePrimitive(token, type);
			if (primitive != null)
			{
				this.valued.setValue(primitive);
				pm.popParser();
				return;
			}
			
			throw new SyntaxError(token, "Invalid Property Value");
		}
		if (this.mode == LIST_END)
		{
			pm.popParser();
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid List - ']' expected", true);
		}
	}
}
