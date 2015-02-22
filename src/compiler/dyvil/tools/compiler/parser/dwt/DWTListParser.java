package dyvil.tools.compiler.parser.dwt;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.Tokens;

public class DWTListParser extends Parser implements IValued
{
	protected IValueList	valueList;
	
	public DWTListParser(IValueList valueList)
	{
		this.valueList = valueList;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.CLOSE_SQUARE_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new DWTValueParser(this), true);
			return;
		}
		if (this.mode == 1)
		{
			if (type == Tokens.COMMA)
			{
				this.mode = 0;
				return;
			}
			
			pm.pushParser(new DWTValueParser(this), true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueList.addValue(value);
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
