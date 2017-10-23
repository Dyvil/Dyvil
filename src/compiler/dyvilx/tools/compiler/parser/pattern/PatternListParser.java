package dyvilx.tools.compiler.parser.pattern;

import dyvilx.tools.compiler.ast.consumer.IPatternConsumer;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.pattern.IPatternList;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public final class PatternListParser extends Parser implements IPatternConsumer
{
	private static final int PATTERN = 0;
	private static final int COMMA   = 1;

	protected IPatternList patternList;

	private IPattern pattern;

	public PatternListParser(IPatternList list)
	{
		this.patternList = list;
		// this.mode = PATTERN;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (BaseSymbols.isCloseBracket(type))
		{
			if (this.pattern != null)
			{
				this.patternList.addPattern(this.pattern);
			}
			pm.popParser(true);
			return;
		}

		switch (this.mode)
		{
		case PATTERN:
			this.mode = COMMA;
			pm.pushParser(new PatternParser(this), true);
			return;
		case COMMA:
			this.mode = PATTERN;
			if (type == BaseSymbols.COMMA)
			{
				this.patternList.addPattern(this.pattern);
				return;
			}
			pm.report(token, "pattern.list.comma");
		}
	}

	@Override
	public void setPattern(IPattern Pattern)
	{
		this.pattern = Pattern;
	}
}
