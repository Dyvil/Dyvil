package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.BindingIfStatement;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class IfStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	protected static final int IF             = 1;
	protected static final int CONDITION_PART = 2;
	protected static final int VARIABLE_VALUE = 3;
	protected static final int SEPARATOR      = 4;
	protected static final int THEN           = 5;
	protected static final int ELSE           = 6;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	protected IfStatement statement;
	protected IVariable   lastVariable;

	// =============== Constructors ===============

	public IfStatementParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		this.mode = IF;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case IF:
			if (type != DyvilKeywords.IF)
			{
				pm.report(token, "if.keyword");
				return;
			}

			this.mode = CONDITION_PART;
			this.statement = new IfStatement(token.raw());
			return;
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
				pm.pushParser(this.expressionParser(this.lastVariable::setValue));
				this.mode = SEPARATOR;
				return;
			}
			pm.report(token, "if.binding.assignment");
			// Fallthrough
		case SEPARATOR:
			if (type == BaseSymbols.COMMA)
			{
				this.mode = CONDITION_PART;
				return;
			}
			// Fallthrough
		case THEN:
			this.mode = ELSE;
			pm.pushParser(new StatementListParser(this.statement::setThen), true);
			return;
		case ELSE:
			final IToken next = token.next();
			if (token.isInferred() && next.type() == DyvilKeywords.ELSE)
			{
				// inferred semicolon between } and else
				return;
			}

			if (type == DyvilKeywords.ELSE)
			{
				final int nextType = next.type();
				if (nextType == DyvilKeywords.IF)
				{
					pm.pushParser(new IfStatementParser(this.statement::setElse));
				}
				else
				{
					pm.pushParser(new StatementListParser(this.statement::setElse));
				}

				this.mode = END;
				return;
			}

			// Fallthrough
		case END:
			this.consumer.accept(this.statement);
			pm.popParser(true);
			return;
		}
	}

	private ExpressionParser expressionParser(Consumer<IValue> consumer)
	{
		return new ExpressionParser(consumer).withFlags(ExpressionParser.IGNORE_STATEMENT);
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
	public IVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}
}
