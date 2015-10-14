package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.parsing.token.IToken;

public class ExceptionListParser extends Parser implements ITypeConsumer
{
	private static final int	TYPE		= 1;
	private static final int	SEPARATOR	= 2;
	
	protected IExceptionList exceptionList;
	
	public ExceptionListParser(IExceptionList list)
	{
		this.exceptionList = list;
		this.mode = TYPE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (type)
		{
		case Symbols.OPEN_CURLY_BRACKET:
		case Symbols.EQUALS:
		case Symbols.SEMICOLON:
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case TYPE:
			pm.pushParser(pm.newTypeParser(this), true);
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			if (type == Symbols.COMMA)
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
