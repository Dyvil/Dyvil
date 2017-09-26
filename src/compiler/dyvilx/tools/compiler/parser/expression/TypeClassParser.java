package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.ClassOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.TypeOperator;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class TypeClassParser extends Parser
{
	private static final int TYPE_CLASS      = 0;
	private static final int TYPE            = 1;
	private static final int PARENTHESES_END = 2;
	private static final int ANGLE_END       = 4;

	private IValueConsumer valueConsumer;

	private IValue value;

	public TypeClassParser(IValueConsumer valueConsumer)
	{
		this.valueConsumer = valueConsumer;

		// this.mode = TYPE_CLASS
	}

	public TypeClassParser(IValueConsumer valueConsumer, IToken token, boolean isClass)
	{
		this.valueConsumer = valueConsumer;

		this.mode = TYPE;
		if (isClass)
		{
			this.value = new ClassOperator(token.raw());
		}
		else
		{
			this.value = new TypeOperator(token.raw());
		}
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE_CLASS:
			if (type == DyvilKeywords.CLASS)
			{
				this.value = new ClassOperator(token.raw());
				this.mode = TYPE;
				return;
			}
			if (type == DyvilKeywords.TYPE)
			{
				this.value = new TypeOperator(token.raw());
				this.mode = TYPE;
				return;
			}

			throw new Error();
		case TYPE:
			if (TypeParser.isGenericStart(token, type))
			{
				this.mode = ANGLE_END;
				pm.splitJump(token, 1);
				pm.pushParser(new TypeParser(this.value, true));
				return;
			}

			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = PARENTHESES_END;
			}
			else
			{
				this.mode = END;
				pm.reparse();
			}

			pm.pushParser(new TypeParser(this.value));
			return;
		case PARENTHESES_END:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, this.isClassOperator() ? "classoperator.close_paren" : "typeoperator.close_paren");
				return;
			}

			this.valueConsumer.setValue(this.value);
			pm.popParser();
			return;
		case ANGLE_END:
			if (!TypeParser.isGenericEnd(token, type))
			{
				pm.report(token, this.isClassOperator() ? "classoperator.close_angle" : "typeoperator.close_angle");
				return;
			}

			this.valueConsumer.setValue(this.value);
			pm.popParser();
			pm.splitJump(token, 1);
			return;
		case END:
			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
		}
	}

	private boolean isClassOperator()
	{
		return this.value.valueTag() == IValue.CLASS_OPERATOR;
	}
}
