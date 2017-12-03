package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.consumer.IMethodConsumer;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.parser.classes.MethodParser;
import dyvilx.tools.gensrc.ast.directive.FuncDirective;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class FuncDirectiveParser extends Parser implements IMethodConsumer
{
	private static final int KEYWORD     = 0;
	private static final int OPEN_PAREN  = 1;
	private static final int CLOSE_PAREN = 3;
	private static final int BODY        = 4;
	private static final int BODY_END    = 5;

	private final StatementList directives;

	private FuncDirective funcDirective;

	public FuncDirectiveParser(StatementList directives)
	{
		this.directives = directives;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case KEYWORD:
			if (type != GenSrcSymbols.FUNC)
			{
				pm.report(token, "func.declarator");
				return;
			}

			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				this.directives.add(this.funcDirective);
				pm.popParser(true);
				return;
			}

			pm.pushParser(new MethodParser(this));
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "func.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.directives.add(this.funcDirective);
				pm.popParser(true);
				return;
			}

			final StatementList body = new StatementList();
			pm.pushParser(new BlockParser(body));
			this.funcDirective.setBlock(body);
			this.mode = BODY_END;
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.directives.add(this.funcDirective);
			pm.popParser();
		}
	}

	@Override
	public void addMethod(IMethod method)
	{
		this.funcDirective = new FuncDirective(method);
	}
}
