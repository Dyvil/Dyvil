package dyvilx.tools.compiler.parser.statement;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.exception.CatchBlock;
import dyvilx.tools.compiler.ast.statement.exception.TryStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class TryStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	private static final int ACTION          = 1;
	private static final int CATCH           = 2;
	private static final int CATCH_ACTION = 16;

	// =============== Fields ===============

	protected final TryStatement statement;

	private CatchBlock catchBlock;

	// =============== Constructors ===============

	public TryStatementParser(TryStatement statement)
	{
		this.statement = statement;
		this.mode = ACTION;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case END:
			pm.popParser(true);
			return;
		case ACTION:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				ForStatementParser.reportSingleStatement(pm, token, "try.single.deprecated");
			}
			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = CATCH;
			return;
		case CATCH: // a catch or finally keyword
			if (type == DyvilKeywords.CATCH)
			{
				this.statement.addCatchBlock(this.catchBlock = new CatchBlock());
				this.mode = CATCH_ACTION;
				pm.pushParser(new DataMemberParser<>(this));
				return;
			}
			if (type == DyvilKeywords.FINALLY)
			{
				final IToken next = token.next();
				if (next.type() != BaseSymbols.OPEN_CURLY_BRACKET)
				{
					ForStatementParser.reportSingleStatement(pm, next, "finally.single.deprecated");
				}

				pm.pushParser(new ExpressionParser(this.statement::setFinallyBlock));
				this.mode = END;
				return;
			}
			if (BaseSymbols.isTerminator(type))
			{
				int nextType = token.next().type();
				if (nextType == Tokens.EOF)
				{
					pm.popParser(true);
					return;
				}
				if (nextType == DyvilKeywords.CATCH || nextType == DyvilKeywords.FINALLY)
				{
					return;
				}
			}
			pm.popParser(true);
			return;
		case CATCH_ACTION: // a block { ... } or semicolon
			switch (type)
			{
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				this.mode = CATCH;
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = CATCH;
				pm.pushParser(new StatementListParser(this.catchBlock::setAction), true);
				return;
			}

			pm.report(token, "try.catch.separator");
		}
	}

	@Override
	public void addDataMember(IVariable variable)
	{
		this.catchBlock.setVariable(variable);
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}
}
