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
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class TryStatementParser extends Parser implements IDataMemberConsumer<IVariable>
{
	// =============== Constants ===============

	private static final int TRY = 1;
	private static final int ACTION = 2;
	private static final int CATCH = 3;
	private static final int CATCH_ACTION = 4;

	// =============== Fields ===============

	private final Consumer<? super TryStatement> consumer;

	private TryStatement statement;
	private CatchBlock catchBlock;

	// =============== Constructors ===============

	public TryStatementParser(Consumer<? super TryStatement> consumer)
	{
		this.consumer = consumer;
		this.mode = TRY;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TRY:
			this.mode = ACTION;
			this.statement = new TryStatement(token.raw());
			if (type != DyvilKeywords.TRY)
			{
				pm.reparse();
				pm.report(token, "try.keyword");
			}
			return;
		case ACTION:
			pm.pushParser(new StatementListParser(this.statement::setAction), true);
			this.mode = CATCH;
			return;
		case CATCH: // a catch or finally keyword
			switch (type)
			{
				case BaseSymbols.SEMICOLON:
					if (token.isInferred())
					{
						final int nextType = token.next().type();
						if (nextType == DyvilKeywords.CATCH || nextType == DyvilKeywords.FINALLY)
						{
							return;
						}
					}
					break; // end
				case DyvilKeywords.CATCH:
					this.statement.addCatchBlock(this.catchBlock = new CatchBlock());
					this.mode = CATCH_ACTION;
					pm.pushParser(new DataMemberParser<>(this));
					return;
				case DyvilKeywords.FINALLY:
					pm.pushParser(new StatementListParser(this.statement::setFinallyBlock));
					this.mode = END;
					return;
			}
			this.end(pm);
			return;
		case CATCH_ACTION: // a block { ... } or semicolon
			this.mode = CATCH;
			pm.pushParser(new StatementListParser(this.catchBlock::setAction), true);
			return;
		case END:
			this.end(pm);
			return;
		}
	}

	private void end(IParserManager pm)
	{
		this.consumer.accept(this.statement);
		pm.popParser(true);
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
