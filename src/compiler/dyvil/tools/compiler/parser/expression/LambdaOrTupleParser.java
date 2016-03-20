package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.expression.TupleExpr;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class LambdaOrTupleParser extends Parser
{
	protected static final int OPEN_PARENTHESIS = 1;
	protected static final int PARAMETERS_END   = 2;
	protected static final int ARROW            = 7;
	protected static final int TUPLE_END        = 4;

	protected IValueConsumer consumer;

	private IValue value;

	public LambdaOrTupleParser(IValueConsumer consumer)
	{
		this.mode = OPEN_PARENTHESIS;
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		switch (this.mode)
		{
		case OPEN_PARENTHESIS:
			// ( ...
			// ^

			/*
			 * The new version of this parser tries to find the matching closing parenthesis instead of try-parsing the
			 * expression. If it finds that parenthesis token and the next token is a lambda arrow, we can assume that
			 * the expression is a lambda expression. Thus, we directly push a Parameter List Parser that may also
			 * produce syntax errors.
			 */

			final IToken closeParenthesis = ParserUtil.findMatch(token);
			if (closeParenthesis != null && closeParenthesis.next().type() == DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				// ( ... ) =>

				final LambdaExpr lambdaExpr = new LambdaExpr(closeParenthesis.next());
				pm.pushParser(new ParameterListParser(lambdaExpr, true));
				this.value = lambdaExpr;
				this.mode = PARAMETERS_END;
				return;
			}

			// ( ... )
			final TupleExpr tupleExpr = new TupleExpr(token);
			pm.pushParser(new ExpressionListParser(tupleExpr));
			this.value = tupleExpr;
			this.mode = TUPLE_END;
			return;
		case TUPLE_END:
			this.value.expandPosition(token);
			this.consumer.setValue(this.value);

			pm.popParser();
			if (token.type() != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "tuple.close_paren");
			}
			return;
		case PARAMETERS_END:
			this.mode = ARROW;
			if (token.type() != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "lambda.close_paren");
			}
			return;
		case ARROW:
			pm.pushParser(pm.newExpressionParser(((LambdaExpr) this.value)));
			this.mode = END;
			if (token.type() != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.reparse();
				pm.report(token, "lambda.arrow");
			}
			return;
		case END:
			pm.popParser(true);
			this.consumer.setValue(this.value);
			return;
		}
	}
}
