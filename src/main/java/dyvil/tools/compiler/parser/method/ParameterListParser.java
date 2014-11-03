package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;

public class ParameterListParser extends Parser
{
	public static final int		TYPE		= 0;
	public static final int		NAME		= 1;
	
	protected IContext			context;
	protected IParameterized	parameterized;
	
	private Parameter			parameter	= new Parameter();
	
	public ParameterListParser(IContext context, IParameterized parameterized)
	{
		this.context = context;
		this.parameterized = parameterized;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(TYPE))
		{
			int i = 0;
			if ((i = Modifiers.PARAMETER.parse(value)) != -1)
			{
				this.parameter.addModifier(i);
				return true;
			}
			else if (token.isType(IToken.TYPE_CLOSE_BRACKET))
			{
				pm.popParser(true);
				return true;
			}
			
			this.mode = NAME;
			pm.pushParser(new TypeParser(this.context, this.parameter), true);
			return true;
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				this.parameter.setName(value);
				return true;
			}
			else if (ParserUtil.isSeperator(value.charAt(0)))
			{
				this.parameter.setSeperator(value.charAt(0));
				this.end(pm);
				this.mode = TYPE;
				return true;
			}
			
			pm.popParser(true);
			return true;
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
