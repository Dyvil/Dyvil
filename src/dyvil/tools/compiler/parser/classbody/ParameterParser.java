package dyvil.tools.compiler.parser.classbody;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.member.Parameter;
import dyvil.tools.compiler.ast.member.Type;
import dyvil.tools.compiler.ast.member.methods.Method;

public class ParameterParser extends Parser
{	
	private Method method;
	
	private String name;
	private Type type;
	
	public ParameterParser(Method method)
	{
		this.method = method;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (this.checkModifier(value))
		{
			;
		}
		else if (")".equals(value))
		{
			jcp.popParser();
		}
		else if (this.type == null)
		{
			this.type = new Type();
			jcp.pushParser(new TypeParser(this.type, ","));
		}
		else if (this.name == null)
		{
			this.name = value;
		}
		else if (",".equals(value))
		{
			this.method.addParameter(new Parameter(this.name, this.type, this.modifiers));
			this.name = null;
			this.type = null;
			this.modifiers = 0;
		}
	}
}
