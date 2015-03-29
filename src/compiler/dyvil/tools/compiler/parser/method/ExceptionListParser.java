package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ParserUtil;

public class ExceptionListParser extends Parser implements ITyped
{
	private static final int	TYPE		= 0;
	private static final int	SEPERATOR	= 1;
	
	protected IExceptionList	exceptionList;
	
	public ExceptionListParser(IExceptionList list)
	{
		this.exceptionList = list;
	}
	
	@Override
	public void reset()
	{
		this.mode = TYPE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == TYPE)
		{
			pm.pushParser(new TypeParser(this), true);
			this.mode = 1;
			return;
		}
		if (this.mode == SEPERATOR)
		{
			if (ParserUtil.isSeperator(type))
			{
				this.mode = 0;
				return;
			}
			
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.exceptionList.addException(type);
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
}
