package dyvilx.tools.compiler.parser.statement;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.token.IToken;

public class RepeatStatementParser extends Parser
{
	// =============== Constants ===============

	protected static final int REPEAT = 1;
	protected static final int ACTION = 2;
	protected static final int WHILE  = 3;

	// =============== Fields ===============

	protected final RepeatStatement statement;

	// =============== Constructors ===============

	public RepeatStatementParser(RepeatStatement statement)
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
		case REPEAT:
			this.mode = ACTION;
			if (type != DyvilKeywords.REPEAT)
			{
				pm.reparse();
				pm.report(token, "repeat.keyword");
			}
			return;
		case ACTION:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				final Marker marker = Markers.syntaxWarning(SourcePosition.before(token), "repeat.single.deprecated");
				marker.addInfo(Markers.getSyntax("statement.single.deprecated.fix"));
				pm.report(marker);
			}

			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = WHILE;
			return;
		case WHILE:
			if (type == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.pushParser(new ExpressionParser(this.statement::setCondition));
				return;
			}
			if (type == BaseSymbols.SEMICOLON && token.isInferred() && token.next().type() == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.skip(1);
				pm.pushParser(new ExpressionParser(this.statement::setCondition));
				return;
			}
			// fallthrough
		case END:
			pm.popParser(true);
		}
	}
}
