package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.MatchCase;
import dyvil.tools.compiler.ast.expression.MatchExpr;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.pattern.PatternParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class MatchExpressionParser extends Parser implements IValueConsumer
{
	private static final int	OPEN_BRACKET	= 1;
	private static final int	CASE			= 2;
	private static final int	CONDITION		= 4;
	private static final int	ACTION			= 8;
	private static final int	SEPARATOR		= 16;
	
	protected MatchExpr matchExpression;
	
	private MatchCase	currentCase;
	private boolean		singleCase;
	
	public MatchExpressionParser(MatchExpr matchExpression)
	{
		this.matchExpression = matchExpression;
		this.mode = OPEN_BRACKET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		
		switch (this.mode)
		{
		case OPEN_BRACKET:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.mode = CASE;
				return;
			}
			if (type != DyvilKeywords.CASE)
			{
				pm.report(token, "Invalid Match Expression - '{' or 'case' expected");
				return;
			}
			this.singleCase = true;
		case CASE:
			if (type == BaseSymbols.SEMICOLON && token.isInferred())
			{
				return;
			}
			if (type == DyvilKeywords.CASE)
			{
				this.currentCase = new MatchCase();
				this.mode = CONDITION;
				pm.pushParser(new PatternParser(this.currentCase));
				return;
			}
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				return;
			}
			pm.report(token, "Invalid Match Expression - 'case' or '}' expected expected");
			return;
		case CONDITION:
			if (type == DyvilKeywords.IF)
			{
				this.mode = ACTION;
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
		case ACTION:
			this.mode = SEPARATOR;
			pm.pushParser(pm.newExpressionParser(this));
			if (type == BaseSymbols.COLON || type == DyvilSymbols.ARROW_OPERATOR)
			{
				return;
			}
			pm.report(token, "Invalid Match Case - ':' or '=>' expected");
			return;
		case SEPARATOR:
			this.matchExpression.addCase(this.currentCase);
			if (this.singleCase)
			{
				pm.popParser(true);
				return;
			}
			
			this.currentCase = null;
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				return;
			}
			this.mode = CASE;
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Match Case - ';' expected");
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case ACTION:
			this.currentCase.setCondition(value);
			return;
		case SEPARATOR:
			this.currentCase.setAction(value);
			return;
		}
	}
}
