package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ExpressionListParser extends Parser implements IValued
{
	protected IContext		context;
	protected IValueList	valueList;
	
	public ExpressionListParser(IContext context, IValueList valueList)
	{
		this.context = context;
		this.valueList = valueList;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new ExpressionParser(this.context, this), true);
			return true;
		}
		else if (this.mode == 1)
		{
			if (",".equals(value))
			{
				this.valueList.setArray(true);
				this.mode = 0;
				return true;
			}
			if (";".equals(value))
			{
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(true);
		return true;
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
