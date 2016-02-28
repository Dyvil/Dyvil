package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueMap;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ExpressionMapParser extends Parser implements IValueConsumer
{
	public static final int NAME      = 1;
	public static final int VALUE     = 2;
	public static final int SEPERATOR = 4;
	
	protected IValueMap valueMap;
	
	private Name key;
	
	public ExpressionMapParser(IValueMap valueMap)
	{
		this.valueMap = valueMap;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case NAME:
			this.mode = VALUE;
			if (ParserUtil.isIdentifier(type) && token.next().type() == BaseSymbols.COLON)
			{
				this.key = token.nameValue();
				pm.skip();
				return;
			}
			this.key = Names.update;
			this.mode = VALUE;
			// Fallthrough
		case VALUE:
			this.mode = SEPERATOR;
			pm.pushParser(pm.newExpressionParser(this), true);
			return;
		case SEPERATOR:
			this.mode = NAME;
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "expression.named_list.comma");
			}
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueMap.addValue(this.key, value);
		this.key = null;
	}
}
