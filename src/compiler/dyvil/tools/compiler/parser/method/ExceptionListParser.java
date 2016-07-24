package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ExceptionListParser extends Parser implements ITypeConsumer
{
	private static final int TYPE      = 1;
	private static final int SEPARATOR = 2;
	
	protected IExceptionList exceptionList;
	
	public ExceptionListParser(IExceptionList list)
	{
		this.exceptionList = list;
		this.mode = TYPE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.EQUALS:
		case BaseSymbols.SEMICOLON:
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case TYPE:
			pm.pushParser(new TypeParser(this), true);
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			this.mode = TYPE;
			if (type != BaseSymbols.COMMA)
			{
				pm.report(token, "method.throws.comma");
				pm.reparse();
			}

			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.exceptionList.addException(type);
	}
}
