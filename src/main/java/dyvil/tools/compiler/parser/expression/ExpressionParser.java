package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.expression.ClassAccess;
import dyvil.tools.compiler.ast.expression.ConstructorCall;
import dyvil.tools.compiler.ast.expression.FieldAccess;
import dyvil.tools.compiler.ast.expression.MethodCall;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.FieldAssign;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.statement.ReturnStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.statement.IfStatementParser;
import dyvil.tools.compiler.parser.type.TypeParser;

public class ExpressionParser extends Parser implements ITyped, IValued
{
	public static final int	VALUE			= 1;
	public static final int	VALUE_2			= 2;
	public static final int	TUPLE_END		= 4;
	
	public static final int	ACCESS			= 8;
	public static final int	DOT_ACCESS		= 16;
	
	public static final int	STATEMENT		= 64;
	public static final int	TYPE			= 128;
	public static final int	PARAMETERS		= 256;
	public static final int	PARAMETERS_2	= 512;
	public static final int	VARIABLE		= 1024;
	
	protected IContext		context;
	protected IValued		field;
	protected boolean		lazy;
	
	private IValue			value;
	
	public ExpressionParser(IContext context, IValued field)
	{
		this.mode = VALUE;
		this.context = context;
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0 || ";".equals(value) || ")".equals(value) && !this.isInMode(PARAMETERS_2) && !this.isInMode(TUPLE_END))
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.isInMode(VALUE))
		{
			if (this.parsePrimitive(value, token))
			{
				this.mode = ACCESS;
				return true;
			}
			else if ("this".equals(value))
			{
				this.mode = ACCESS;
				this.value = new ThisValue(token, this.context.getThisType());
				return true;
			}
			else if ("super".equals(value))
			{
				this.mode = ACCESS;
				this.value = new SuperValue(token, this.context.getThisType());
				return true;
			}
			else if ("return".equals(value))
			{
				ReturnStatement statement = new ReturnStatement(token);
				this.value = statement;
				pm.pushParser(new ExpressionParser(this.context, statement));
				return true;
			}
			else if ("if".equals(value))
			{
				IfStatement statement = new IfStatement(token);
				this.value = statement;
				pm.pushParser(new IfStatementParser(this.context, statement));
				return true;
			}
			else if ("(".equals(value))
			{
				this.mode = TUPLE_END;
				this.value = new TupleValue();
				
				if (!token.next().equals(")"))
				{
					pm.pushParser(new ExpressionListParser(this.context, (IValueList) this.value));
				}
				return true;
			}
			else if ("{".equals(value))
			{
				this.mode = VALUE_2;
				this.value = new StatementList(token);
				
				if (!token.next().equals("}"))
				{
					pm.pushParser(new ExpressionListParser(this.context, (IValueList) this.value));
				}
				return true;
			}
			else if ("new".equals(value))
			{
				ConstructorCall call = new ConstructorCall(token);
				this.mode = PARAMETERS;
				this.value = call;
				pm.pushParser(new TypeParser(this.context, call));
				return true;
			}
			else if ((token.isType(IToken.TYPE_IDENTIFIER) || token.equals("(")) && !token.next().isType(IToken.TYPE_OPEN_BRACKET))
			{
				this.mode = ACCESS | VARIABLE;
				pm.pushParser(new TypeParser(this.context, this), true);
				return true;
			}
			this.mode = ACCESS;
		}
		if (this.isInMode(VALUE_2))
		{
			if ("}".equals(value))
			{
				this.value.expandPosition(token);
				this.mode = ACCESS;
				return true;
			}
		}
		if (this.isInMode(TUPLE_END))
		{
			if (")".equals(value))
			{
				this.value.expandPosition(token);
				this.mode = ACCESS;
				return true;
			}
		}
		if (this.isInMode(VARIABLE))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				IToken next = token.next();
				boolean flag = next.equals("=");
				if (flag || next.equals(";"))
				{
					ICodePosition pos = token.raw();
					Type type = ((ClassAccess) this.value).getType();
					
					FieldAssign access = new FieldAssign(pos, value, null);
					access.field = new Variable(pos, value, type);
					access.initializer = true;
					this.value = access;
					
					if (flag)
					{
						pm.skip();
						pm.pushParser(new ExpressionParser(this.context, access));
					}
					
					return true;
				}
			}
		}
		if (this.isInMode(ACCESS))
		{
			if (".".equals(value))
			{
				this.mode = DOT_ACCESS;
				return true;
			}
			
			if ("=".equals(value))
			{
				String name = null;
				IValue instance = null;
				if (this.value instanceof ClassAccess)
				{
					name = ((ClassAccess) this.value).getName();
				}
				else if (this.value instanceof FieldAccess)
				{
					FieldAccess fa = (FieldAccess) this.value;
					name = fa.getName();
					instance = fa.getValue();
				}
				else
				{
					return false;
				}
				
				FieldAssign assign = new FieldAssign(this.value.getPosition(), name, instance);
				this.value = assign;
				pm.pushParser(new ExpressionParser(this.context, assign));
				return true;
			}
			
			if (this.lazy && this.value != null)
			{
				pm.popParser(true);
				return true;
			}
			this.mode = DOT_ACCESS;
		}
		if (this.isInMode(DOT_ACCESS))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				IToken next = token.next();
				if (next.isType(IToken.TYPE_OPEN_BRACKET))
				{
					MethodCall call = new MethodCall(token, this.value, value);
					this.value = call;
					this.mode = PARAMETERS;
					return true;
				}
				else if (!next.isType(IToken.TYPE_IDENTIFIER) && !next.isType(IToken.TYPE_CLOSE_BRACKET) && !next.isType(IToken.TYPE_SYMBOL))
				{
					MethodCall call = new MethodCall(token, this.value, value);
					call.setSugar(true);
					this.value = call;
					
					ExpressionParser parser = new ExpressionParser(this.context, this);
					parser.lazy = true;
					pm.pushParser(parser);
					return true;
				}
				else
				{
					FieldAccess access = new FieldAccess(token, this.value, value);
					this.value = access;
					this.mode = ACCESS;
					return true;
				}
			}
		}
		if (this.isInMode(PARAMETERS))
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ExpressionListParser(this.context, (IValueList) this.value));
				this.mode = PARAMETERS_2;
				return true;
			}
		}
		if (this.isInMode(PARAMETERS_2))
		{
			if (")".equals(value))
			{
				this.value.expandPosition(token);
				this.mode = ACCESS;
				return true;
			}
		}
		
		if (this.value != null)
		{
			this.value.expandPosition(token);
			pm.popParser(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void end(ParserManager pm)
	{
		if (this.value != null)
		{
			this.field.setValue(this.value);
		}
	}
	
	public boolean parsePrimitive(String value, IToken token) throws SyntaxError
	{
		if ("null".equals(value))
		{
			this.value = new NullValue();
			return true;
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
		else if (token.isType(IToken.TYPE_STRING))
		{
			this.value = new StringValue((String) token.object());
			return true;
		}
		// Char
		else if (token.isType(IToken.TYPE_CHAR))
		{
			this.value = new CharValue((Character) token.object());
			return true;
		}
		// Int
		else if (token.isType(IToken.TYPE_INT))
		{
			this.value = new IntValue((Integer) token.object());
			return true;
		}
		else if (token.isType(IToken.TYPE_LONG))
		{
			this.value = new LongValue((Long) token.object());
			return true;
		}
		// Float
		else if (token.isType(IToken.TYPE_FLOAT))
		{
			this.value = new FloatValue((Float) token.object());
			return true;
		}
		else if (token.isType(IToken.TYPE_DOUBLE))
		{
			this.value = new DoubleValue((Double) token.object());
			return true;
		}
		return false;
	}
	
	@Override
	public void setType(Type type)
	{
		this.value = new ClassAccess(type.getPosition(), type);
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public void setValue(IValue value)
	{
		((MethodCall) this.value).addValue(value);
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
