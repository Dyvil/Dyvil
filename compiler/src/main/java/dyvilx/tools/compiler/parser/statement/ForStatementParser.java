package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.loop.ForEachStatement;
import dyvilx.tools.compiler.ast.statement.loop.IForStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ForStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	private static final int FOR = 1;
	private static final int VARIABLE = 2;
	private static final int VARIABLE_SEPARATOR = 3;
	private static final int STATEMENT = 4;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private IForStatement forStatement;

	// =============== Constructors ===============

	public ForStatementParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		this.mode = FOR;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case FOR:
			this.mode = VARIABLE;
			this.forStatement = new ForEachStatement(token.raw());
			if (type != DyvilKeywords.FOR)
			{
				pm.reparse();
				pm.report(token, "for.keyword");
			}
			return;
		case VARIABLE:
			pm.pushParser(new DataMemberParser<>(this), true);
			this.mode = VARIABLE_SEPARATOR;
			return;
		case VARIABLE_SEPARATOR:
			if (type != DyvilSymbols.ARROW_LEFT)
			{
				pm.reparse();
				pm.report(token, "for.variable.separator");
			}

			this.mode = STATEMENT;

			pm.pushParser(new ExpressionParser(this.forStatement.getVariable()::setValue)
				              .withFlags(ExpressionParser.IGNORE_CLOSURE));
			return;
		case STATEMENT:
			pm.pushParser(new StatementListParser(this.forStatement::setAction), true);
			this.mode = END;
			return;
			// Fallthrough
		case END:
			pm.popParser(true);
			this.consumer.accept(this.forStatement);
		}
	}

	@Override
	public void addDataMember(IVariable dataMember)
	{
		this.forStatement.setVariable(dataMember);
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}
}
