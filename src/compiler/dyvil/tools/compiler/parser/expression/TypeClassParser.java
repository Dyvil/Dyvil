package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.ClassOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.TypeOperator;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

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
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = PARENTHESES_END;
			}
			else if (TypeParser.isGenericStart(token, type))
			{
				this.mode = ANGLE_END;
				pm.splitJump(token, 1);
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
				pm.reparse();
				pm.report(token, this.value.valueTag() == IValue.CLASS_OPERATOR ?
					                 "classoperator.close_paren" :
					                 "typeoperator.close_paren");
			}

			this.valueConsumer.setValue(this.value);
			pm.popParser();
			return;
		case ANGLE_END:
			this.valueConsumer.setValue(this.value);
			pm.popParser();

			if (TypeParser.isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.reparse();
			pm.report(token, this.value.valueTag() == IValue.CLASS_OPERATOR ?
				                 "classoperator.close_angle" :
				                 "typeoperator.close_angle");
			return;
		case END:
			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
		}
	}
}
