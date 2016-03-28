package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.ClassConstructor;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

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

			this.call = new ConstructorCall(token);
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
				this.call.setArguments(ExpressionParser.parseArguments(pm, token.next()));
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
		this.call.expandPosition(token);
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
		final IClassBody body = nestedClass.getBody();

		pm.pushParser(new ClassBodyParser(body), true);
		this.mode = ANONYMOUS_CLASS_END;
	}
}
