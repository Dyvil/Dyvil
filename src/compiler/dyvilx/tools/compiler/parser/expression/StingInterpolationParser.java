package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.StringInterpolationExpr;
import dyvilx.tools.compiler.ast.expression.constant.StringValue;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class StingInterpolationParser extends Parser
{
	// =============== Fields ===============

	protected final StringInterpolationExpr value;

	// =============== Constructors ===============

	public StingInterpolationParser(StringInterpolationExpr value)
	{
		this.value = value;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case Tokens.STRING_START:
		case Tokens.STRING_PART:
		{
			int nextType = token.next().type();
			if (nextType == Tokens.STRING_PART || nextType == Tokens.STRING_END)
			{
				pm.report(token.next(), "stringinterpolation.expression");
				return;
			}
			this.value.append(new StringValue(token.raw(), token.stringValue()));
			pm.pushParser(new ExpressionParser(this.value::append));
			return;
		}
		case Tokens.STRING_END:
			this.value.append(new StringValue(token.raw(), token.stringValue()));
			pm.popParser();
			return;
		}
		pm.reparse();
		pm.report(token, "stringinterpolation.part");
	}
}
