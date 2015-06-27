package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.IPatternList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class PatternListParser extends Parser implements IPatternConsumer
{
	protected IPatternList	patternList;
	
	private IPattern		pattern;
	
	public PatternListParser(IPatternList list)
	{
		this.patternList = list;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
		this.pattern = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
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
		
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new PatternParser(this), true);
			return;
		}
		if (this.mode == 1)
		{
			this.mode = 0;
			if (type == Symbols.COMMA)
			{
				this.patternList.addPattern(this.pattern);
				return;
			}
			throw new SyntaxError(token, "Invalid Pattern List - ',' expected");
		}
	}
	
	@Override
	public void setPattern(IPattern Pattern)
	{
		this.pattern = Pattern;
	}
}
