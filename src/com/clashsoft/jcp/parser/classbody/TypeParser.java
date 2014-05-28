package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.member.Type;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

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
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
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
