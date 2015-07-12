package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;

public class ExceptionListParser extends Parser implements ITypeConsumer
{
	private static final int	TYPE		= 1;
	private static final int	SEPARATOR	= 2;
	
	protected IExceptionList	exceptionList;
	
	public ExceptionListParser(IExceptionList list)
	{
		this.exceptionList = list;
		this.mode = TYPE;
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
			pm.pushParser(pm.newTypeParser(this), true);
			this.mode = SEPARATOR;
			return;
		}
		if (this.mode == SEPARATOR)
		{
			if (ParserUtil.isSeperator(type))
			{
				this.mode = TYPE;
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
}
