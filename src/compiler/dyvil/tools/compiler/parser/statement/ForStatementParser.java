package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.ForStatement;
import dyvil.tools.compiler.ast.statement.foreach.ForEachStatement;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class ForStatementParser extends Parser implements IValueConsumer
{
	private static final int	FOR				= 1;
	private static final int	FOR_START		= 2;
	private static final int	TYPE			= 4;
	private static final int	VARIABLE		= 8;
	private static final int	SEPERATOR		= 16;
	private static final int	VARIABLE_END	= 32;
	private static final int	CONDITION_END	= 64;
	private static final int	FOR_END			= 128;
	private static final int	STATEMENT		= 256;
	private static final int	STATEMENT_END	= 512;
	
	protected IValueConsumer field;
	
	private ICodePosition	position;
	private Variable		variable;
	private IValue			update;
	private IValue			condition;
	private IValue			action;
	private boolean			forEach;
	
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
	
	private IValue makeForStatement()
	{
		if (this.forEach)
		{
			return new ForEachStatement(this.position, this.variable, this.action);
		}
		return new ForStatement(this.position, this.variable, this.condition, this.update, this.action);
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case FOR:
			this.mode = FOR_START;
			if (type != Keywords.FOR)
			{
				pm.reparse();
				pm.report(token, "Invalid For Statement - 'for' expected");
			}
			return;
		case FOR_START:
			this.mode = TYPE;
			if (type != Symbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid For Statement - '(' expected");
			}
			return;
		case TYPE:
			if (type == Symbols.SEMICOLON)
			{
				// Condition
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			this.variable = new Variable();
			pm.pushParser(pm.newTypeParser(this.variable), true);
			this.mode = VARIABLE;
			return;
		case VARIABLE:
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				this.variable.setName(token.nameValue());
				this.variable.setPosition(token.raw());
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid For statement - Variable Name expected");
			return;
		case SEPERATOR:
			if (type == Symbols.COLON)
			{
				this.mode = FOR_END;
				this.forEach = true;
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}
			this.mode = VARIABLE_END;
			if (type == Symbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid For Statement - ';' or ':' expected");
			return;
		case VARIABLE_END:
			this.mode = CONDITION_END;
			if (type == Symbols.SEMICOLON)
			{
				if (token.next().type() == Symbols.SEMICOLON)
				{
					return;
				}
				
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid for statement - ';' expected");
			return;
		case CONDITION_END:
			this.mode = FOR_END;
			if (type != Symbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "Invalid for statement - ';' expected");
				return;
			}
			
			if (token.next().type() != Symbols.SEMICOLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
			}
			
			return;
		case FOR_END:
			this.mode = STATEMENT;
			if (type != Symbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid for statement - ')' expected");
			}
			return;
		case STATEMENT:
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				this.field.setValue(this.makeForStatement());
				return;
			}
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = STATEMENT_END;
			return;
		case STATEMENT_END:
			pm.popParser(true);
			this.field.setValue(this.makeForStatement());
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
		case FOR_END:
			if (this.forEach)
			{
				this.variable.setValue(value);
			}
			else
			{
				this.update = value;
			}
			return;
		case STATEMENT_END:
			this.action = value;
			return;
		}
	}
}
