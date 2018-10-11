package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.MapExpr;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ArrayLiteralParser extends Parser
{
	// =============== Constants ===============

	protected static final int OPEN_BRACKET = 1;
	protected static final int SEPARATOR    = 2;
	protected static final int COLON        = 4;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private IToken startPosition;

	private ArgumentList keys = new ArgumentList();
	private ArgumentList values;

	// =============== Constructors ===============

	public ArrayLiteralParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		this.mode = OPEN_BRACKET;
	}

	// =============== Static Methods ===============

	private static ExpressionParser expressionParser(ArgumentList list)
	{
		return new ExpressionParser(list::add).withFlags(ExpressionParser.IGNORE_COLON);
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case OPEN_BRACKET:
			pm.pushParser(expressionParser(this.keys));
			this.mode = SEPARATOR | COLON;
			this.startPosition = token;

			if (type != BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "array.open_bracket");
			}
			return;
		case SEPARATOR | COLON:
			if (type == BaseSymbols.COLON)
			{
				this.mode = SEPARATOR;
				this.values = new ArgumentList(this.keys.size());
				pm.pushParser(expressionParser(this.values));
				return;
			}
			// Fallthrough
		case SEPARATOR:
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}

			this.mode = this.values != null ? COLON : SEPARATOR;
			pm.pushParser(expressionParser(this.keys));
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.report(token, "array.separator");
			}
			return;
		case COLON:
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}

			this.mode = SEPARATOR;
			pm.pushParser(expressionParser(this.values));
			if (type != BaseSymbols.COLON)
			{
				pm.reparse();
				pm.report(token, "array.map.colon");
			}
			return;
		case END:
		}
	}

	private void end(IToken token)
	{
		if (this.values != null)
		{
			final MapExpr map = new MapExpr(this.startPosition.to(token), this.keys, this.values);
			this.consumer.accept(map);
			return;
		}

		final ArrayExpr array = new ArrayExpr(this.startPosition.to(token), this.keys);
		this.consumer.accept(array);
	}
}
