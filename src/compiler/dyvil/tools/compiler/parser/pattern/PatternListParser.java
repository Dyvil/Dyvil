package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.IPatternList;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class PatternListParser extends Parser implements IPatternConsumer
{
	protected IPatternList patternList;
	
	private IPattern pattern;
	
	public PatternListParser(IPatternList list)
	{
		this.patternList = list;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
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
		case 0:
			this.mode = 1;
			pm.pushParser(new PatternParser(this), true);
			return;
		case 1:
			this.mode = 0;
			if (type == Symbols.COMMA)
			{
				this.patternList.addPattern(this.pattern);
				return;
			}
			pm.report(token, "Invalid Pattern List - ',' expected");
			return;
		}
	}
	
	@Override
	public void setPattern(IPattern Pattern)
	{
		this.pattern = Pattern;
	}
}
