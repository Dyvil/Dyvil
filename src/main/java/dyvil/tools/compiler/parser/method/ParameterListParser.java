package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;

public class ParameterListParser extends Parser
{
	public static final int	TYPE		= 0;
	public static final int	NAME		= 1;
	
	private IParameterized	parameterized;
	
	private Parameter		parameter	= new Parameter();
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(TYPE))
		{
			int i = 0;
			if ((i = Modifiers.PARAMETER.parse(value)) != -1)
			{
				this.parameter.addModifier(i);
				return true;
			}
			else if (")".equals(value))
			{
				jcp.popParser();
				return true;
			}
			
			this.mode = NAME;
			jcp.pushParser(new TypeParser(this.parameter), token);
			return true;
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(Token.TYPE_IDENTIFIER))
			{
				this.parameter.setName(value);
				return true;
			}
			else if (ParserUtil.isSeperatorChar(value))
			{
				this.parameter.setSeperator(value.charAt(0));
				this.end(jcp);
				this.mode = TYPE;
				return true;
			}
			else if (")".equals(value))
			{
				jcp.popParser(token);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.parameter.hasName())
		{
			this.parameterized.addParameter(this.parameter);
			this.parameter = new Parameter();
		}
	}
}
