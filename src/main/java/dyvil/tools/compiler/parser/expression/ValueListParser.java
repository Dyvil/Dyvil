package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.context.IClassContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;

public class ValueListParser extends Parser implements IValued
{
	protected IClassContext	context;
	protected IValueList	valueList;
	
	/**
	 * Current Value to parse.
	 */
	private IValue			value;
	
	public ValueListParser(IClassContext context, IValueList valueList)
	{
		this.context = context;
		this.valueList = valueList;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			pm.pushParser(new ValueParser(this.context, this), token);
			this.mode = 1;
		}
		else
		{
			this.mode = 0;
			this.valueList.addValue(this.value);
			this.value = null;
			
			// End of Value List
			if (!ParserUtil.isSeperatorChar(value))
			{
				pm.popParser(token);
			}
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
}
