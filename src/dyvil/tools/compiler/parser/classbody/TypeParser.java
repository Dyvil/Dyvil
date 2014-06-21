package dyvil.tools.compiler.parser.classbody;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.member.Type;

public class TypeParser extends Parser
{
	public static final int	TYPE	= 0;
	public static final int	ARRAY	= 1;
	public static final int	GENERIC	= 2;
	
	private Type			type;
	private String			endOn;
	private int				mode;
	
	public TypeParser(Type type, String endOn)
	{
		this.type = type;
		this.endOn = endOn;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (value.equals(this.endOn))
		{
			jcp.popParser();
		}
		else if (this.mode == TYPE)
		{
			this.type.setClassName(value);
		}
		else if (value.startsWith("["))
		{
			this.mode = ARRAY;
			this.type.incrArrayDimensions();
		}
		else if ("<".equals(value))
		{
			
		}
	}
}
