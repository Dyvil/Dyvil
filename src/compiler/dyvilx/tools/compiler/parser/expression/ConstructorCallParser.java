package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.expression.access.ClassConstructor;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.parser.classes.ClassBodyParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class ConstructorCallParser extends Parser
{
	private static final int NEW                        = 0;
	private static final int CONSTRUCTOR_PARAMETERS     = 1;
	private static final int CONSTRUCTOR_PARAMETERS_END = 2;
	private static final int ANONYMOUS_CLASS_END        = 4;
	private static final int ANONYMOUS_CLASS            = 8;

	protected IValueConsumer consumer;

	private ConstructorCall call;

	public ConstructorCallParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case NEW:
			if (type != DyvilKeywords.NEW)
			{
				pm.report(token, "constructor.new");
				pm.reparse();
			}

			final ArgumentList arguments =
				token.next().type() == BaseSymbols.OPEN_PARENTHESIS ? null : ArgumentList.empty();
			this.call = new ConstructorCall(token.raw(), arguments);
			this.mode = CONSTRUCTOR_PARAMETERS;
			pm.pushParser(new TypeParser(this.call));
			return;
		case CONSTRUCTOR_PARAMETERS:
			// new ...
			//         ^
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				// new ... (
				this.mode = CONSTRUCTOR_PARAMETERS_END;
				ArgumentListParser.parseArguments(pm, token.next(), this.call);
				return;
			}
			// Fallthrough
		case ANONYMOUS_CLASS:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				// new ... ( ... ) { ...
				this.parseBody(pm);
				return;
			}

			pm.reparse();
			this.end(pm, token.prev());
			return;
		case CONSTRUCTOR_PARAMETERS_END:
			// new ... ( ... )
			//               ^
			this.mode = ANONYMOUS_CLASS;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "constructor.call.close_paren");
			}
			return;
		case ANONYMOUS_CLASS_END:
			// new ... { ... } ...
			//               ^
			this.end(pm, token);

			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "class.anonymous.body.end");
			}
		}
	}

	public void end(IParserManager pm, IToken token)
	{
		this.call.setPosition(this.call.getPosition().to(token));
		this.consumer.setValue(this.call);
		pm.popParser();
	}

	/**
	 * Creates the body and initializes parsing for anonymous classes.
	 *
	 * @param pm
	 * 	the current parsing context manager.
	 */
	private void parseBody(IParserManager pm)
	{
		final ClassConstructor classConstructor = this.call.toClassConstructor();
		this.call = classConstructor;

		final IClass nestedClass = classConstructor.getNestedClass();
		final ClassBody body = nestedClass.getBody();

		pm.pushParser(new ClassBodyParser(body), true);
		this.mode = ANONYMOUS_CLASS_END;
	}
}
