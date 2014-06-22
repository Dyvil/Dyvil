package dyvil.tools.compiler.parser.method;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ParameterListParser extends Parser
{	
	private IParameterized parameterized;
	
	private Parameter parameter;
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (")".equals(value))
		{
			this.parameterized.addParameter(this.parameter);
			jcp.popParser();
		}
		else if (this.checkModifier(value))
		{
		}
		else if (this.parameter.getType() == null)
		{
			jcp.pushParser(new TypeParser(this.parameterized));
		}
		else if (this.parameter.getName() == null)
		{
			this.parameter.setName(value);
		}
		else if (",".equals(value) || ";".equals(value) || ":".equals(value))
		{
			this.parameter.setSeperator(value.charAt(0));
			this.parameterized.addParameter(this.parameter);
		}
	}
}
