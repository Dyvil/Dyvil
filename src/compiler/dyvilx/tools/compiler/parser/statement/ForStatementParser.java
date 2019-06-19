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
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ForStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	private static final int FOR                = 0;
	private static final int FOR_START          = 1;
	private static final int VARIABLE           = 1 << 1;
	private static final int VARIABLE_SEPARATOR = 1 << 2;
	private static final int FOR_EACH_END       = 1 << 6;
	private static final int STATEMENT          = 1 << 7;

	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	private IForStatement forStatement;

	private boolean parenthesis;

	// =============== Constructors ===============

	public ForStatementParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		// this.mode = FOR;
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
			this.forStatement = new ForEachStatement(token.raw());
			if (type != DyvilKeywords.FOR)
			{
				pm.reparse();
				pm.report(token, "for.keyword");
			}
			return;
		case FOR_START:
			this.mode = VARIABLE;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.parenthesis = true;
				pm.report(Markers.syntaxWarning(token, "for.paren.deprecated"));
				return;
			}
			// Fallthrough
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

			this.mode = FOR_EACH_END;

			pm.pushParser(new ExpressionParser(this.forStatement.getVariable()::setValue)
				              .withFlags(this.parenthesis ? 0 : ExpressionParser.IGNORE_CLOSURE));
			return;
		case FOR_EACH_END:
			this.mode = STATEMENT;
			if (this.parenthesis)
			{
				if (type != BaseSymbols.CLOSE_PARENTHESIS)
				{
					pm.reparse();
					pm.report(token, "for.close_paren");
				}
				return;
			}
			// Fallthrough
		case STATEMENT:
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
			default:
				reportSingleStatement(pm, token, "for.single.deprecated");
				pm.pushParser(new ExpressionParser(this.forStatement::setAction), true);
				this.mode = END;
				return;
			}
		case END:
			pm.popParser(true);
			this.consumer.accept(this.forStatement);
		}
	}

	static void reportSingleStatement(IParserManager pm, IToken token, String key)
	{
		final Marker marker = Markers.syntaxWarning(SourcePosition.before(token), key);
		marker.addInfo(Markers.getSyntax("statement.single.deprecated.fix"));
		pm.report(marker);
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
