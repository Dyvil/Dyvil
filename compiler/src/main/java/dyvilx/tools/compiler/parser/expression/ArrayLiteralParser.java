package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.DummyValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.MapExpr;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ArrayLiteralParser extends Parser
{
	// =============== Constants ===============

	protected static final int OPEN_BRACKET     = 1;
	protected static final int FIRST_EXPRESSION = 2;
	protected static final int FIRST_COLON      = 3;

	protected static final int ARRAY_ELEMENT   = 4;
	protected static final int ARRAY_SEPARATOR = 5;

	protected static final int MAP_KEY       = 6;
	protected static final int MAP_COLON     = 7;
	protected static final int MAP_VALUE     = 8;
	protected static final int MAP_SEPARATOR = 9;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private IToken startToken;

	private ArgumentList keys;
	private ArgumentList values = new ArgumentList();

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
		if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
		{
			pm.popParser();
			if (this.keys != null)
			{
				if (this.values.size() < this.keys.size())
				{
					pm.report(Markers.syntaxError(token, "expression.expected", token));
					this.values.add(DummyValue.INSTANCE);
				}

				this.consumer.accept(new MapExpr(this.startToken.to(token), this.keys, this.values));
			}
			else
			{
				this.consumer.accept(new ArrayExpr(this.startToken.to(token), this.values));
			}
			return;
		}

		switch (this.mode)
		{
		case OPEN_BRACKET:
			if (type != BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.report(token, "array.open_bracket");
			}

			this.mode = FIRST_EXPRESSION;
			this.startToken = token;
			return;
		case FIRST_EXPRESSION:
			if (type == BaseSymbols.COLON && token.next().type() == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				// [ : ]
				this.keys = new ArgumentList();
				this.mode = END;
				return;
			}

			pm.pushParser(expressionParser(this.values), true);
			this.mode = FIRST_COLON;
			return;
		case FIRST_COLON:
			if (type == BaseSymbols.COLON)
			{
				// [ expr :
				this.mode = MAP_VALUE;
				this.keys = this.values;
				this.values = new ArgumentList();
				return;
			}
			// Fallthrough
		case ARRAY_SEPARATOR:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (!token.isInferred())
				{
					pm.report(token, "array.separator");
				}
				// Fallthrough
			case BaseSymbols.COMMA:
				this.mode = ARRAY_ELEMENT;
				return;
			default:
				pm.report(token, "array.separator");
				return;
			}
		case ARRAY_ELEMENT:
			this.mode = ARRAY_SEPARATOR;
			pm.pushParser(expressionParser(this.values), true);
			return;
		case MAP_KEY:
			pm.pushParser(expressionParser(this.keys), true);
			this.mode = MAP_COLON;
			return;
		case MAP_COLON:
			if (type != BaseSymbols.COLON)
			{
				pm.report(token, "map.colon");
				return;
			}

			this.mode = MAP_VALUE;
			return;
		case MAP_VALUE:
			pm.pushParser(expressionParser(this.values), true);
			this.mode = MAP_SEPARATOR;
			return;
		case MAP_SEPARATOR:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (!token.isInferred())
				{
					pm.report(token, "map.separator");
				}
				// Fallthrough
			case BaseSymbols.COMMA:
				this.mode = MAP_KEY;
				return;
			default:
				pm.report(token, "map.separator");
				return;
			}
		}
	}
}
