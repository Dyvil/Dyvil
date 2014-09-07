package dyvil.tools.compiler.parser.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IClassContext;
import dyvil.tools.compiler.ast.expression.ClassAccess;
import dyvil.tools.compiler.ast.expression.FieldAccess;
import dyvil.tools.compiler.ast.expression.MethodCall;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ParserUtil;

public class ValueParser extends Parser implements IValued, IValueList
{
	public static final int	TYPE		= 1;
	public static final int	PARAMETERS	= 2;
	public static final int PARAMETERS_END = 3;
	
	protected IClassContext	context;
	protected IValued		field;
	
	private IValue value;
	private IMethod method;
	
	private List<IValue> arguments = new ArrayList();
	
	private StringBuilder tempClass = new StringBuilder();
	
	public ValueParser(IClassContext context, IValued field)
	{
		this.context = context;
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		IField field;
		IClass iclass;
		if (this.parsePrimitive(value, token))
		{
			return true;
		}
		else if ((field = this.context.resolveField(value)) != null)
		{	
			this.value = new FieldAccess(this.value, field);
		}
		else if ((this.method = this.context.resolveMethodName(value)) != null)
		{
			this.mode = PARAMETERS;
		}
		else if ((iclass = this.context.resolveClass(this.tempClass.toString())) != null)
		{
			this.tempClass = null;
			this.value = new ClassAccess(iclass);
		}
		// Constructors
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
		// Parameters
		else if ("(".equals(token))
		{
			if (this.mode == PARAMETERS)
			{
				jcp.pushParser(new ValueListParser(this.context, this));
				this.mode = PARAMETERS_END;
			}
		}
		// Single Argument, short Parameter notation
		else if (this.mode == PARAMETERS)
		{
			jcp.pushParser(new ValueParser(this.context, this));
			this.mode = PARAMETERS_END;
		}
		else if (this.mode == PARAMETERS_END)
		{
			Type[] types = ParserUtil.getTypes(this.arguments);
			// Properly resolve the method
			this.method = this.context.resolveMethod(this.method.getName(), types);
			this.value = new MethodCall(this.value, this.method, this.arguments);
		}
		// Used for extended class names
		else if (this.tempClass != null)
		{
			this.tempClass.append(value);
		}
		this.field.setValue(this.value);
		jcp.popParser();
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
		// Int
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
		else if (token.type() == Token.TYPE_INT_BIN)
		{
			if (token.next().equals("L"))
			{
				this.field.setValue(new LongValue(value, 2));
			}
			else
			{
				this.field.setValue(new IntValue(value, 2));
			}
		}
		else if (token.type() == Token.TYPE_INT_HEX)
		{
			if (token.next().equals("L"))
			{
				this.field.setValue(new LongValue(value, 16));
			}
			else
			{
				this.field.setValue(new IntValue(value, 16));
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
			// FIXME
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
