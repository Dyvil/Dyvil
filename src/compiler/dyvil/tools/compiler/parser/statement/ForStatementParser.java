package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvil.tools.compiler.ast.statement.loop.ForStatement;
import dyvil.tools.compiler.ast.statement.loop.IForStatement;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public class ForStatementParser extends Parser implements IValueConsumer, ITypeConsumer
{
	private static final int FOR           = 1;
	private static final int FOR_START     = 2;
	private static final int TYPE          = 4;
	private static final int VARIABLE_NAME = 8;
	private static final int SEPARATOR     = 16;
	private static final int VARIABLE_END  = 32;
	private static final int CONDITION_END = 64;
	private static final int FOR_END       = 128;
	private static final int FOR_EACH_END  = 256;
	private static final int STATEMENT     = 512;
	private static final int STATEMENT_END = 1024;

	protected IValueConsumer field;

	private   ICodePosition position;
	private   IType         type;
	private   Variable      variable;
	private   IValue        update;
	private   IValue        condition;
	protected IForStatement forStatement;

	public ForStatementParser(IValueConsumer field)
	{
		this.field = field;
		this.mode = FOR;
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
			this.mode = TYPE;
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "for.open_paren");
			}
			return;
		case TYPE:
			if (type == BaseSymbols.SEMICOLON)
			{
				// Condition
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			if (type == DyvilKeywords.VAR)
			{
				this.mode = VARIABLE_NAME;
				this.type = Types.UNKNOWN;
				return;
			}

			pm.pushParser(pm.newTypeParser(this), true);
			this.mode = VARIABLE_NAME;
			return;
		case VARIABLE_NAME:
			this.mode = SEPARATOR;
			if (ParserUtil.isIdentifier(type))
			{
				this.variable = new Variable(token.raw(), token.nameValue(), this.type);
				return;
			}
			pm.reparse();
			pm.report(token, "for.variable.identifier");
			return;
		case SEPARATOR:
			if (type == BaseSymbols.COLON)
			{
				pm.report(Markers.syntaxWarning(token, "for.each.colon.deprecated"));
				this.mode = FOR_EACH_END;
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}

			if (type == DyvilSymbols.ARROW_LEFT || type == Tokens.LETTER_IDENTIFIER)
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
			return;
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
			return;
		}
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
