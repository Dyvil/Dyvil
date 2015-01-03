package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class StatementListParser extends ExpressionListParser
{
	public StatementListParser(IContext context, IValueList valueList)
	{
		super(context, valueList);
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
		else if (";".equals(value))
		{
			this.mode = 0;
			return true;
		}
		
		pm.popParser(true);
		return true;
	}
}
