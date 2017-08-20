package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.statement.BindingIfStatement;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.statement.VariableStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class IfStatementParser extends Parser implements IValueConsumer, IDataMemberConsumer<IVariable>
{
	protected static final int IF             = 0;
	protected static final int VARIABLE_VALUE = 1;
	protected static final int THEN           = 2;
	protected static final int ELSE           = 3;

	protected final IValueConsumer consumer;

	protected IfStatement statement;
	private   IVariable   variable;

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

			if (token.next().type() == DyvilKeywords.LET)
			{
				// if let ...
				this.mode = VARIABLE_VALUE;
				this.statement = new BindingIfStatement(token.raw());
				pm.pushParser(new DataMemberParser<>(this));
				return;
			}

			this.mode = THEN;
			this.statement = new IfStatement(token.raw());
			pm.pushParser(new ExpressionParser(this).withFlags(IGNORE_STATEMENT));
			return;
		case VARIABLE_VALUE:
			this.mode = THEN;
			if (this.variable != null)
			{
				this.statement.setCondition(new VariableStatement(this.variable));
			}

			if (type != BaseSymbols.EQUALS)
			{
				pm.report(token, "if.binding.assignment");
				pm.reparse();
				return;
			}

			pm.pushParser(new ExpressionParser(this.variable).withFlags(IGNORE_STATEMENT));
			return;
		case THEN:
			switch (type)
			{
			case Tokens.EOF:
			case BaseSymbols.SEMICOLON:
				this.end(pm);
				return;
			}

			this.mode = ELSE;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		case ELSE:
			if (type == DyvilKeywords.ELSE)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = END;
				return;
			}
			if (token.isInferred() && token.next().type() == DyvilKeywords.ELSE)
			{
				// ... inferred_semicolon else
				pm.skip();
				pm.pushParser(new ExpressionParser(this));
				this.mode = END;
				return;
			}

			// Fallthrough
		case END:
			this.end(pm);
			return;
		}
	}

	private void end(IParserManager pm)
	{
		this.consumer.setValue(this.statement);
		pm.popParser(true);
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case THEN:
			this.statement.setCondition(value);
			return;
		case ELSE:
			this.statement.setThen(value);
			return;
		case END:
			this.statement.setElse(value);
		}
	}

	@Override
	public void addDataMember(IVariable dataMember)
	{
		this.variable = dataMember;
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                 AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}
}
