package dyvil.tools.compiler.parser.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IClassContext;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ExpressionParser extends Parser implements IValued, IValueList
{
	public static final int	VALUE			= 0;
	public static final int	TYPE			= 1;
	public static final int	PARAMETERS		= 2;
	public static final int	PARAMETERS_END	= 4;
	
	protected IClassContext	context;
	protected IValued		field;
	
	private IValue			value;
	
	private List<IValue>	arguments		= new ArrayList();
	
	public ExpressionParser(IClassContext context, IValued field)
	{
		this.context = context;
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		IField field;
		IClass iclass;
		if (this.parsePrimitive(value, token))
		{
			return true;
		}
		else
		{
			pm.popParser(token);
			return true;
		}
	}
	
	@Override
	public void end(ParserManager pm)
	{
		this.field.setValue(this.value);
	}
	
	public boolean parsePrimitive(String value, IToken token) throws SyntaxError
	{
		if ("null".equals(value))
		{
			this.value = IValue.NULL;
		}
		// Boolean
		else if ("true".equals(value))
		{
			this.value = BooleanValue.of(true);
			return true;
		}
		else if ("false".equals(value))
		{
			this.value = BooleanValue.of(false);
			return true;
		}
		// String
		else if (token.type() == Token.TYPE_STRING)
		{
			this.value = new StringValue((String) token.object());
			return true;
		}
		// Char
		else if (token.type() == Token.TYPE_CHAR)
		{
			this.value = new CharValue((Character) token.object());
			return true;
		}
		// Int
		else if (token.type() == Token.TYPE_INT || token.type() == Token.TYPE_INT_BIN || token.type() == Token.TYPE_INT_HEX)
		{
			if (token.next().equals("L"))
			{
				this.value = new LongValue((Long) token.object());
			}
			else
			{
				this.value = new IntValue((Integer) token.object());
			}
			return true;
		}
		// Float
		else if (token.type() == Token.TYPE_FLOAT || token.type() == Token.TYPE_FLOAT_HEX)
		{
			if (token.next().equals("D"))
			{
				this.value = new DoubleValue((Double) token.object());
			}
			else
			{
				this.value = new FloatValue((Float) token.object());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments.set(0, value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.arguments.get(0);
	}
}
