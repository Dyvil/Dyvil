package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.member.Value;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class ValueParser extends Parser
{
	private static Object	NULL	= new Object();
	
	public static final int TYPE = 1;
	public static final int PARAMETERS = 2;
	
	private int mode;
	
	private Value			value;
	private String			endOn;
	
	private String type;
	
	public ValueParser(Value value, String endOn)
	{
		this.value = value;
		this.endOn = endOn;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (this.endOn.equals(value))
		{
			jcp.popParser();
		}
		else if (parsePrimitive(value))
		{
			return;
		}
		else if ("new".equals(value))
		{
			this.mode = TYPE;
		}
		else if (this.mode == TYPE)
		{
			//jcp.pushParser(new TypeParser("("));
		}
	}
	
	public boolean parsePrimitive(String token)
	{
		// Boolean
		if ("true".equals(token))
		{
			this.value.setBoolean(true);
			return true;
		}
		else if ("false".equals(token))
		{
			this.value.setBoolean(false);
			return true;
		}
		// String
		else if (token.startsWith("\"") && token.endsWith("\""))
		{
			String string = token.substring(1, token.length() - 1);
			this.value.setObject(string);
			return true;
		}
		// Char
		else if (token.startsWith("'") && token.endsWith("'"))
		{
			char c = token.charAt(1);
			this.value.setChar(c);
			return true;
		}
		// Float
		else if (token.endsWith("F"))
		{
			String s = token.replace("_|F", "");
			this.value.setFloat(Float.parseFloat(s));
		}
		// Double
		else if (token.endsWith("D"))
		{
			String s = token.replace("_|D", "");
			this.value.setDouble(Double.parseDouble(s));
		}
		// Long
		else if (token.endsWith("L"))
		{
			if (token.startsWith("0x"))
			{
				String s = token.replace("0x|_|L", "");
				this.value.setLong(Long.parseLong(s, 16));
			}
			else if (token.startsWith("0b"))
			{
				String s = token.replace("0b|_|L", "");
				this.value.setLong(Long.parseLong(s, 2));
			}
			else
			{
				String s = token.replace("_|L", "");
				this.value.setLong(Long.parseLong(s));
			}
		}
		// Integer
		return false;
	}
}
