package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ParameterListParser extends Parser
{
	public static final int	TYPE		= 0;
	public static final int	NAME		= 1;
	public static final int	POST_NAME	= 2;
	
	private IParameterized	parameterized;
	
	private Parameter		parameter;
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (")".equals(value))
		{
			jcp.popParser();
			this.parameterized.addParameter(this.parameter);
			return true;
		}
		else if (",".equals(value) || ";".equals(value) || ":".equals(value))
		{
			if (this.mode == POST_NAME)
			{
				this.parameter.setSeperator(value.charAt(0));
				this.parameterized.addParameter(this.parameter);
				this.mode = TYPE;
				return true;
			}
		}
		else if (this.mode == TYPE)
		{
			this.parameter = new Parameter();
			jcp.pushParser(new TypeParser(this.parameter), token);
			this.mode = NAME;
			return true;
		}
		else if (this.mode == NAME)
		{
			this.parameter.setName(value);
			this.mode = POST_NAME;
		}
		return false;
	}
}
