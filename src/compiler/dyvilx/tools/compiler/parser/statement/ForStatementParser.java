package dyvilx.tools.compiler.parser.statement;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvilx.tools.compiler.ast.statement.loop.ForStatement;
import dyvilx.tools.compiler.ast.statement.loop.IForStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

public class ForStatementParser extends Parser implements IValueConsumer, IDataMemberConsumer<IVariable>
{
	private static final int FOR                = 0;
	private static final int FOR_START          = 1;
	private static final int VARIABLE           = 1 << 1;
	private static final int VARIABLE_SEPARATOR = 1 << 2;
	private static final int VARIABLE_END       = 1 << 3;
	private static final int CONDITION_END      = 1 << 4;
	private static final int FOR_END            = 1 << 5;
	private static final int FOR_EACH_END       = 1 << 6;
	private static final int STATEMENT          = 1 << 7;

	protected IValueConsumer field;

	private   SourcePosition position;
	private   IVariable     variable;
	private   IValue        update;
	private   IValue        condition;
	protected IForStatement forStatement;

	private boolean parenthesis;

	public ForStatementParser(IValueConsumer field)
	{
		this.field = field;
		// this.mode = FOR;
	}

	public ForStatementParser(IValueConsumer field, SourcePosition position)
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
				pm.report(token, "for.for_keyword");
			}
			return;
		case FOR_START:
			this.mode = VARIABLE;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.parenthesis = true;
				return;
			}
			// Fallthrough
		case VARIABLE:
			if (type == BaseSymbols.SEMICOLON)
			{
				// for (; ...
				// => No variable declaration, parse Condition next
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}

			// for ( i ...
			// for ( var i ...
			// for ( let i ...
			pm.pushParser(new DataMemberParser<>(this), true);
			this.mode = VARIABLE_SEPARATOR;
			return;
		case VARIABLE_SEPARATOR:
			switch (type)
			{
			case DyvilSymbols.ARROW_LEFT:
				this.mode = FOR_EACH_END;
				final ExpressionParser parser = new ExpressionParser(this.variable);
				if (!this.parenthesis)
				{
					parser.addFlags(IGNORE_COLON | IGNORE_CLOSURE);
				}
				pm.pushParser(parser);
				return;
			case BaseSymbols.EQUALS:
				this.mode = VARIABLE_END;
				pm.pushParser(new ExpressionParser(this.variable));
				return;
			}

			this.mode = VARIABLE_END;
			pm.reparse();
			pm.report(token, "for.variable.separator");
			return;
		case VARIABLE_END:
			this.mode = CONDITION_END;
			if (token.next().type() != BaseSymbols.SEMICOLON)
			{
				pm.pushParser(new ExpressionParser(this));
				return;
			}
			if (type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "for.variable.semicolon");
			}
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
				final ExpressionParser parser = new ExpressionParser(this);
				if (!this.parenthesis)
				{
					parser.addFlags(IGNORE_COLON | IGNORE_CLOSURE);
				}
				pm.pushParser(parser);
			}

			return;
		case FOR_END:
			this.forStatement = new ForStatement(this.position, this.variable, this.condition, this.update);
			this.parseEnd(pm, token, type);
			return;
		case FOR_EACH_END:
			this.forStatement = new ForEachStatement(this.position, this.variable);
			this.parseEnd(pm, token, type);
			return;
		case STATEMENT:
			if (BaseSymbols.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				this.field.setValue(this.forStatement);
				return;
			}
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
			this.field.setValue(this.forStatement);
		}
	}

	public void parseEnd(IParserManager pm, IToken token, int type)
	{
		if (this.parenthesis)
		{
			this.mode = STATEMENT;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "for.close_paren");
			}
			return;
		}

		switch (type)
		{
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			pm.reparse();
			// Fallthrough
		case Tokens.EOF:
			pm.popParser();
			this.field.setValue(this.forStatement);
			return;
		case BaseSymbols.OPEN_CURLY_BRACKET:
			pm.pushParser(new StatementListParser(this), true);
			this.mode = END;
			return;
		}

		pm.report(token, "for.separator");
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
		case END:
			this.forStatement.setAction(value);
		}
	}

	@Override
	public void addDataMember(IVariable dataMember)
	{
		this.variable = dataMember;
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}
}
