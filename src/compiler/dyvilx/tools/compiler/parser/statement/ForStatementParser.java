package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvilx.tools.compiler.ast.statement.loop.ForStatement;
import dyvilx.tools.compiler.ast.statement.loop.IForStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

public class ForStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	private static final int FOR                = 0;
	private static final int FOR_START          = 1;
	private static final int VARIABLE           = 1 << 1;
	private static final int VARIABLE_SEPARATOR = 1 << 2;
	private static final int VARIABLE_END       = 1 << 3;
	private static final int CONDITION_END      = 1 << 4;
	private static final int FOR_END            = 1 << 5;
	private static final int FOR_EACH_END       = 1 << 6;
	private static final int STATEMENT          = 1 << 7;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private   SourcePosition position;
	private   IVariable      variable;
	private   IValue         condition;
	private   IValue         update;
	protected IForStatement  forStatement;

	private boolean parenthesis;

	// =============== Constructors ===============

	public ForStatementParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		// this.mode = FOR;
	}

	// =============== Properties ===============

	private void setCondition(IValue condition)
	{
		this.condition = condition;
	}

	private void setUpdate(IValue update)
	{
		this.update = update;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case FOR:
			this.mode = FOR_START;
			this.position = token.raw();
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
				pm.pushParser(new ExpressionParser(this::setCondition));
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
				final ExpressionParser parser = new ExpressionParser(this.variable::setValue);
				if (!this.parenthesis)
				{
					parser.addFlags(IGNORE_COLON | IGNORE_CLOSURE);
				}
				pm.pushParser(parser);
				return;
			case BaseSymbols.EQUALS:
				this.mode = VARIABLE_END;
				pm.pushParser(new ExpressionParser(this.variable::setValue));
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
				// parse condition
				pm.pushParser(new ExpressionParser(this::setCondition));
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
				// parse update
				final ExpressionParser parser = new ExpressionParser(this::setUpdate);
				if (!this.parenthesis)
				{
					parser.addFlags(IGNORE_COLON | IGNORE_CLOSURE);
				}
				pm.pushParser(parser);
			}

			return;
		case FOR_END:
			pm.report(Markers.syntaxWarning(this.position, "for.c_style.deprecated"));

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
				this.consumer.accept(this.forStatement);
				return;
			}
			pm.pushParser(new ExpressionParser(this.forStatement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
			this.consumer.accept(this.forStatement);
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
			this.consumer.accept(this.forStatement);
			return;
		case BaseSymbols.OPEN_CURLY_BRACKET:
			pm.pushParser(new StatementListParser(this.forStatement::setAction), true);
			this.mode = END;
			return;
		}

		pm.report(token, "for.separator");
	}

	@Override
	public void addDataMember(IVariable dataMember)
	{
		this.variable = dataMember;
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}
}
