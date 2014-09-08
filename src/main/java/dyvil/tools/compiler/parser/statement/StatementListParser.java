package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.context.IClassContext;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class StatementListParser extends ExpressionListParser
{
	public StatementListParser(IClassContext context, IValueList valueList)
	{
		super(context, valueList, true);
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new ExpressionParser(this.context, this, true), token);
			return true;
		}
		else if (";".equals(value))
		{
			this.mode = 0;
			return true;
		}
		
		pm.popParser(token);
		return true;
	}
}
