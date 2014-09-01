package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ValueParser extends Parser
{
	public static final int	TYPE		= 1;
	public static final int	PARAMETERS	= 2;
	
	private IField			field;
	
	public ValueParser(IField field)
	{
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
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
			jcp.pushParser(new TypeParser((ITyped) this.field));
			return true;
		}
		else if ("(".equals(token))
		{
			if (this.mode == PARAMETERS)
			{
				jcp.pushParser(new ValueListParser());
			}
		}
		return false;
	}
	
	public boolean parsePrimitive(String value, IToken token) throws SyntaxError
	{
		if ("null".equals(value))
		{
			this.field.setValue(null);
		}
		// Boolean
		else if ("true".equals(value))
		{
			this.field.setValue(BooleanValue.of(true));
			return true;
		}
		else if ("false".equals(value))
		{
			this.field.setValue(BooleanValue.of(false));
			return true;
		}
		// String
		else if (token.type() == Token.TYPE_STRING)
		{
			String string = value.substring(1, value.length() - 1);
			this.field.setValue(new StringValue(string));
			return true;
		}
		// Char
		else if (token.type() == Token.TYPE_CHAR)
		{
			this.field.setValue(new CharValue(value));
			return true;
		}
		else if (token.type() == Token.TYPE_INT)
		{
			if (token.next().equals("L"))
			{
				this.field.setValue(new LongValue(value));
			}
			else
			{
				this.field.setValue(new IntValue(value));
			}
		}
		// Float
		else if (token.type() == Token.TYPE_FLOAT)
		{
			if (token.next().equals("D"))
			{
				this.field.setValue(new DoubleValue(value));
			}
			else
			{
				this.field.setValue(new FloatValue(value));
			}
		}
		else if (token.type() == Token.TYPE_FLOAT_HEX)
		{
		}
		return false;
	}
}
