package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;

public class ExpressionListParser extends Parser implements IValued
{
	protected IContext	context;
	protected IValueList	valueList;
	protected boolean		statements;
	
	public ExpressionListParser(IContext context, IValueList valueList)
	{
		this.context = context;
		this.valueList = valueList;
	}
	
	public ExpressionListParser(IContext context, IValueList valueList, boolean statements)
	{
		this.context = context;
		this.valueList = valueList;
		this.statements = statements;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushTryParserParse(new ExpressionParser(this.context, this, this.statements), token);
			return true;
		}
		else if (ParserUtil.isSeperatorChar(value))
		{
			this.mode = 0;
			return true;
		}
		
		pm.popParser(token);
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
