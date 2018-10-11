package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LambdaExpr;
import dyvilx.tools.compiler.ast.expression.TupleLikeExpr;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.BracketMatcher;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.method.ParameterListParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class LambdaOrTupleParser extends Parser
{
	protected static final int OPEN_PARENTHESIS = 0;
	protected static final int TUPLE            = 1;
	protected static final int PARAMETERS_END   = 1 << 1;
	protected static final int TYPE_ARROW       = 1 << 2;
	protected static final int RETURN_ARROW     = 1 << 3;
	protected static final int TUPLE_END        = 1 << 4;
	protected static final int SINGLE_PARAMETER = 1 << 5;

	protected IValueConsumer consumer;

	private IValue value;

	public LambdaOrTupleParser(IValueConsumer consumer)
	{
		this.mode = OPEN_PARENTHESIS;
		this.consumer = consumer;
	}

	public LambdaOrTupleParser(IValueConsumer consumer, int mode)
	{
		this.mode = mode;
		this.consumer = consumer;
	}

	public LambdaOrTupleParser(IValueConsumer consumer, boolean tupleOnly)
	{
		this.mode = tupleOnly ? TUPLE : OPEN_PARENTHESIS;
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
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

			final IToken closeParen = BracketMatcher.findMatch(token);
			if (closeParen != null)
			{
				final IToken next = closeParen.next();
				final int nextType = next.type();
				if (nextType == DyvilSymbols.ARROW_RIGHT || nextType == DyvilSymbols.DOUBLE_ARROW_RIGHT)
				{
					// (     ... )          =>
					// (     ... )          ->
					// token     closeParen next

					final LambdaExpr lambdaExpr = new LambdaExpr(next);
					pm.pushParser(new ParameterListParser(lambdaExpr));
					this.value = lambdaExpr;
					this.mode = PARAMETERS_END;
					return;
				}
			}
			// Fallthrough
		case TUPLE:
			// ( ... )
			final TupleLikeExpr tupleExpr = new TupleLikeExpr(token);
			pm.pushParser(new ArgumentListParser(tupleExpr::setValues));
			this.value = tupleExpr;
			this.mode = TUPLE_END;
			return;
		case TUPLE_END:
			this.value.expandPosition(token);
			this.consumer.setValue(this.value);

			pm.popParser();
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "tuple.close_paren");
			}
			return;
		case PARAMETERS_END:

			this.mode = TYPE_ARROW;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "lambda.close_paren");
			}
			return;
		case SINGLE_PARAMETER:
			if (Tokens.isIdentifier(type))
			{
				final LambdaExpr lambdaExpr = new LambdaExpr(token.next());
				final IParameter parameter = lambdaExpr.createParameter(token.raw(), token.nameValue(), Types.UNKNOWN,
				                                                        new AttributeList());
				lambdaExpr.getParameters().add(parameter);
				this.value = lambdaExpr;
				this.mode = TYPE_ARROW;
				return;
			}
			// Fallthrough
		case TYPE_ARROW:
			if (this.value == null)
			{
				this.value = new LambdaExpr(token.raw());
			}
			if (type == DyvilSymbols.ARROW_RIGHT)
			{
				pm.pushParser(returnTypeParser((LambdaExpr) this.value));
				this.mode = RETURN_ARROW;
				return;
			}
			// Fallthrough
		case RETURN_ARROW:
			pm.pushParser(new ExpressionParser(((LambdaExpr) this.value)));
			this.mode = END;
			if (type != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.reparse();
				pm.report(token, "lambda.arrow");
			}
			return;
		case END:
			pm.popParser(true);
			this.consumer.setValue(this.value);
		}
	}

	public static TypeParser returnTypeParser(LambdaExpr value)
	{
		return new TypeParser(value::setReturnType).withFlags(TypeParser.IGNORE_LAMBDA);
	}
}
