package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.consumer.IVariableConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvil.tools.compiler.ast.statement.loop.ForStatement;
import dyvil.tools.compiler.ast.statement.loop.IForStatement;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public class ForStatementParser extends Parser implements IValueConsumer, IVariableConsumer
{
	private static final int FOR           = 0;
	private static final int FOR_START     = 1;
	private static final int VARIABLE      = 1 << 1;
	private static final int SEPARATOR     = 1 << 2;
	private static final int VARIABLE_END  = 1 << 3;
	private static final int CONDITION_END = 1 << 4;
	private static final int FOR_END       = 1 << 5;
	private static final int FOR_EACH_END  = 1 << 6;
	private static final int STATEMENT     = 1 << 7;
	private static final int STATEMENT_END = 1 << 8;

	protected IValueConsumer field;

	private   ICodePosition position;
	private   IVariable     variable;
	private   IValue        update;
	private   IValue        condition;
	protected IForStatement forStatement;

	public ForStatementParser(IValueConsumer field)
	{
		this.field = field;
		// this.mode = FOR;
	}

	public ForStatementParser(IValueConsumer field, ICodePosition position)
	{
		this.field = field;
		this.position = position;
		this.mode = FOR_START;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case FOR:
			this.mode = FOR_START;
			if (type != DyvilKeywords.FOR)
			{
				pm.reparse();
				pm.report(token, "for.for");
			}
			return;
		case FOR_START:
			this.mode = VARIABLE;
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "for.open_paren");
			}
			return;
		case VARIABLE:
			if (type == BaseSymbols.SEMICOLON)
			{
				// Condition
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			if (ParserUtil.isIdentifier(type) && token.next().type() == DyvilSymbols.ARROW_LEFT)
			{
				// for ( i <- ...
				this.variable = new Variable(token.raw(), token.nameValue(), Types.UNKNOWN);
				this.mode = SEPARATOR;
				return;
			}

			pm.pushParser(new VariableParser(this), true);
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			if (type == DyvilSymbols.ARROW_LEFT)
			{
				this.mode = FOR_EACH_END;
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}

			this.mode = VARIABLE_END;
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}
			pm.reparse();
			pm.report(token, "for.variable.separator");
			return;
		case VARIABLE_END:
			this.mode = CONDITION_END;
			if (type == BaseSymbols.SEMICOLON)
			{
				if (token.next().type() == BaseSymbols.SEMICOLON)
				{
					return;
				}

				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			pm.reparse();
			pm.report(token, "for.variable.semicolon");
			return;
		case CONDITION_END:
			this.mode = FOR_END;
			if (type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "for.condition.semicolon");
				return;
			}

			if (token.next().type() != BaseSymbols.SEMICOLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
			}

			return;
		case FOR_END:
			this.mode = STATEMENT;
			this.forStatement = new ForStatement(this.position, this.variable, this.condition, this.update);
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "for.close_paren");
			}
			return;
		case FOR_EACH_END:
			this.mode = STATEMENT;
			this.forStatement = new ForEachStatement(this.position, this.variable);
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "for.each.close_paren");
			}
			return;
		case STATEMENT:
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				this.field.setValue(this.forStatement);
				return;
			}
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = STATEMENT_END;
			return;
		case STATEMENT_END:
			pm.popParser(true);
			this.field.setValue(this.forStatement);
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case VARIABLE_END:
			this.variable.setValue(value);
			return;
		case CONDITION_END:
			this.condition = value;
			return;
		case FOR_EACH_END:
			this.variable.setValue(value);
			return;
		case FOR_END:
			this.update = value;
			return;
		case STATEMENT_END:
			this.forStatement.setAction(value);
		}
	}

	@Override
	public void setVariable(IVariable variable)
	{
		this.variable = variable;
	}
}
