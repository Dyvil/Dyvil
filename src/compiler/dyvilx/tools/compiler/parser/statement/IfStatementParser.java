package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.statement.BindingIfStatement;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class IfStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	protected static final int IF             = 0;
	protected static final int OPEN_PAREN     = 1;
	protected static final int CONDITION_PART = 2;
	protected static final int VARIABLE_VALUE = 3;
	protected static final int SEPARATOR      = 4;
	protected static final int THEN           = 5;
	protected static final int ELSE           = 6;

	protected final IValueConsumer consumer;

	protected IfStatement statement;
	protected IVariable   lastVariable;
	protected boolean     parentheses;

	public IfStatementParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = IF;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case IF:
			if (type != DyvilKeywords.IF)
			{
				pm.report(token, "if.if_keyword");
				return;
			}

			this.mode = OPEN_PAREN;
			this.statement = new IfStatement(token.raw());
			return;
		case OPEN_PAREN:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.parentheses = true;
				this.mode = CONDITION_PART;
				return;
			}
			// Fallthrough
		case CONDITION_PART:
			if (type == DyvilKeywords.LET)
			{
				pm.pushParser(new DataMemberParser<>(this), true);
				this.mode = VARIABLE_VALUE;
				return;
			}

			this.mode = SEPARATOR;
			pm.pushParser(this.expressionParser(this.statement::setCondition), true);
			return;
		case VARIABLE_VALUE:
			if (type == BaseSymbols.EQUALS)
			{
				pm.pushParser(this.expressionParser(this.lastVariable));
				this.mode = SEPARATOR;
				return;
			}
			pm.report(token, "if.binding.assignment");
			// Fallthrough
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
				if (this.parentheses && !this.hasCondition())
				{
					this.mode = CONDITION_PART;
					return;
				}
				break; // then
			case BaseSymbols.CLOSE_PARENTHESIS:
				if (this.parentheses)
				{
					this.mode = THEN;
					return;
				}
				break; // then
			}

			if (this.parentheses)
			{
				pm.report(token, "if.close_paren");
			}
			// Fallthrough
		case THEN:
			switch (type)
			{
			case Tokens.EOF:
			case BaseSymbols.SEMICOLON:
				this.end(pm);
				return;
			}

			this.mode = ELSE;
			pm.pushParser(new ExpressionParser(this.statement::setThen), true);
			return;
		case ELSE:
			if (type == DyvilKeywords.ELSE)
			{
				pm.pushParser(new ExpressionParser(this.statement::setElse));
				this.mode = END;
				return;
			}
			if (token.isInferred() && token.next().type() == DyvilKeywords.ELSE)
			{
				// ... inferred_semicolon else
				pm.skip();
				pm.pushParser(new ExpressionParser(this.statement::setElse));
				this.mode = END;
				return;
			}

			// Fallthrough
		case END:
			this.end(pm);
			return;
		}
	}

	private boolean hasCondition()
	{
		final IValue condition = this.statement.getCondition();
		return condition != null && condition != BooleanValue.TRUE;
	}

	private ExpressionParser expressionParser(IValueConsumer consumer)
	{
		return new ExpressionParser(consumer).withFlags(this.parentheses ? 0 : IGNORE_STATEMENT);
	}

	private void end(IParserManager pm)
	{
		this.consumer.setValue(this.statement);
		pm.popParser(true);
	}

	@Override
	public void addDataMember(IVariable dataMember)
	{
		BindingIfStatement statement;
		if (!(this.statement instanceof BindingIfStatement))
		{
			this.statement = statement = new BindingIfStatement(this.statement.getPosition());
		}
		else
		{
			statement = (BindingIfStatement) this.statement;
		}
		this.lastVariable = dataMember;
		statement.addVariable(dataMember);
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                 AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}
}
