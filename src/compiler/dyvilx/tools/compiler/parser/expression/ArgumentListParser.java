package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.NamedArgumentList;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class ArgumentListParser extends Parser implements IValueConsumer
{
	private static final int NAME      = 0;
	private static final int VALUE     = 1;
	private static final int SEPARATOR = 2;

	private final IArgumentsConsumer consumer;

	private Name name;

	private Name[] names;
	private IValue[] values = new IValue[2];
	private int valueCount;

	public ArgumentListParser(IArgumentsConsumer consumer)
	{
		this.consumer = consumer;
		// this.mode = NAME
	}

	private void end(IParserManager pm)
	{
		if (this.names == null)
		{
			this.consumer.setArguments(new ArgumentList(this.values, this.valueCount));
		}
		else
		{
			this.consumer.setArguments(new NamedArgumentList(this.names, this.values, this.valueCount));
		}

		pm.popParser(true);
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (BaseSymbols.isCloseBracket(type) || type == Tokens.EOF)
		{
			if (this.name != null)
			{
				pm.report(token, "arguments.expression");
			}

			this.end(pm);
			return;
		}

		switch (this.mode)
		{
		case NAME:
			this.mode = VALUE;
			this.name = null;
			if (token.next().type() == BaseSymbols.COLON)
			{
				if (Tokens.isIdentifier(type))
				{
					this.name = token.nameValue();
					pm.skip();
					return;
				}
				else if (Tokens.isKeyword(type)) // TODO do not accept modifier keywords
				{
					this.name = Name.fromRaw(token.stringValue());
					pm.skip();
					return;
				}
			}

			this.mode = VALUE;
			// Fallthrough
		case VALUE:
			this.mode = SEPARATOR;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		case SEPARATOR:
			this.mode = NAME;
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "arguments.separator");
			}
		}
	}

	public static void parseArguments(IParserManager pm, IToken next, IArgumentsConsumer consumer)
	{
		final int nextType = next.type();

		if (BaseSymbols.isCloseBracket(nextType))
		{
			consumer.setArguments(ArgumentList.empty());
			return;
		}

		pm.pushParser(new ArgumentListParser(consumer));
	}

	@Override
	public void setValue(IValue value)
	{
		final int index = this.valueCount++;
		if (index >= this.values.length)
		{
			if (this.names != null)
			{
				final Name[] tempNames = new Name[this.valueCount];
				System.arraycopy(this.names, 0, tempNames, 0, index);
				this.names = tempNames;
			}

			final IValue[] tempValues = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, tempValues, 0, index);
			this.values = tempValues;
		}

		this.values[index] = value;

		if (this.name == null)
		{
			return;
		}

		if (this.names == null)
		{
			this.names = new Name[Math.max(this.valueCount, 2)];
		}

		this.names[index] = this.name;
		this.name = null;
	}
}
