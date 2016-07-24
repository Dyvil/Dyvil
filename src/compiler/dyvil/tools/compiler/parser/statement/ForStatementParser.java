package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvil.tools.compiler.ast.statement.loop.ForStatement;
import dyvil.tools.compiler.ast.statement.loop.IForStatement;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.classes.DataMemberParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

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

	private   ICodePosition position;
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
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.parenthesis = true;
				return;
			}
			// Fallthrough
		case VARIABLE:
			if (type == BaseSymbols.SEMICOLON)
			{
				// Condition
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			if (ParserUtil.isIdentifier(type) && token.next().type() == DyvilSymbols.ARROW_LEFT)
			{
				// for ( i <- ...
				this.variable = new Variable(token.raw(), token.nameValue(), Types.UNKNOWN);
				this.mode = VARIABLE_SEPARATOR;
				return;
			}

			pm.pushParser(new DataMemberParser<>(this).ignoringTypeAscription(), true);
			this.mode = VARIABLE_SEPARATOR;
			return;
		case VARIABLE_SEPARATOR:
			switch (type)
			{
			case BaseSymbols.COLON:
				pm.report(Markers.syntaxWarning(token, "for.variable.colon.deprecated"));
				// Fallthrough
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
			if (ParserUtil.isTerminator(type) && !token.isInferred())
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
		case BaseSymbols.COLON:
			this.mode = END;
			pm.pushParser(new ExpressionParser(this));
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
	public IVariable createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}
}
