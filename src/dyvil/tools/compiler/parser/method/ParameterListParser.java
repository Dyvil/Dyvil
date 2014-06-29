package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;

public class ParameterListParser extends Parser
{
	public static final int	TYPE	= 0;
	public static final int	NAME	= 1;
	public static final int	END		= 2;
	
	private IParameterized	parameterized;
	
	private Parameter		parameter;
	private int				modifiers;
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		int i = 0;
		if ((i = Modifiers.parseParameterModifier(value)) != 0)
		{
			this.modifiers |= i;
		}
		else if (")".equals(value))
		{
			jcp.popParser();
			this.parameterized.addParameter(this.parameter);
			return true;
		}
		else if (",".equals(value) || ";".equals(value) || ":".equals(value))
		{
			if (this.mode == END)
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
			this.parameter.setModifiers(this.modifiers);
			this.mode = NAME;
			this.modifiers = 0;
			jcp.pushParser(new TypeParser(this.parameter), token);
			return true;
		}
		else if (this.mode == NAME)
		{
			this.parameter.setName(value);
			this.mode = END;
		}
		return false;
	}
}
