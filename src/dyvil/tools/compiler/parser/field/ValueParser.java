package dyvil.tools.compiler.parser.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ValueParser extends Parser
{
	public static final int	TYPE		= 1;
	public static final int	PARAMETERS	= 2;
	
	private int				mode;
	
	private IField			field;
	
	public ValueParser(IField field)
	{
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (this.parsePrimitive(value, token))
		{
			jcp.popParser();
			return true;
		}
		else if ("new".equals(value))
		{
			this.mode = TYPE;
			return true;
		}
		else if (this.mode == TYPE)
		{
			this.mode = PARAMETERS;
			jcp.pushParser(new TypeParser(this.field));
			return true;
		}
		else if (this.mode == PARAMETERS)
		{
			
		}
		return false;
	}
	
	public boolean parsePrimitive(String value, IToken token) throws SyntaxException
	{
		if ("null".equals(value))
		{
			this.field.setValue(null);
		}
		// Boolean
		else if ("true".equals(value))
		{
			this.field.setValue(Boolean.TRUE);
			return true;
		}
		else if ("false".equals(value))
		{
			this.field.setValue(Boolean.FALSE);
			return true;
		}
		// String
		else if (token.type() == Token.TYPE_STRING)
		{
			String string = value.substring(1, value.length() - 1);
			this.field.setValue(string);
			return true;
		}
		// Char
		else if (token.type() == Token.TYPE_CHAR)
		{
			char c = value.charAt(1);
			this.field.setValue(Character.valueOf(c));
			return true;
		}
		else if (token.type() == Token.TYPE_INT)
		{
			if (token.next().equals("L"))
			{
				this.field.setValue(Long.valueOf(value));
			}
			else
			{
				this.field.setValue(Integer.valueOf(value));
			}
		}
		// Float
		else if (token.type() == Token.TYPE_FLOAT)
		{
			if (token.next().equals("D"))
			{
				this.field.setValue(Double.valueOf(value));
			}
			else
			{
				this.field.setValue(Float.valueOf(value));
			}
		}
		else if (token.type() == Token.TYPE_FLOAT_HEX)
		{
		}
		return false;
	}
}
