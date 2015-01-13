package dyvil.tools.compiler.parser.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.expression.ClassAccess;
import dyvil.tools.compiler.ast.expression.ConstructorCall;
import dyvil.tools.compiler.ast.expression.FieldAccess;
import dyvil.tools.compiler.ast.expression.MethodCall;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.BytecodeParser;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.statement.IfStatementParser;
import dyvil.tools.compiler.parser.statement.WhileStatementParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.OperatorComparator;

public class ExpressionParser extends Parser implements ITyped, IValued
{
	public static final int	VALUE			= 1;
	public static final int	LIST_END		= 2;
	public static final int	TUPLE_END		= 4;
	
	public static final int	ACCESS			= 8;
	public static final int	ACCESS_2		= 16;
	
	public static final int	LAMBDA			= 32;
	public static final int	STATEMENT		= 64;
	public static final int	TYPE			= 128;
	public static final int	PARAMETERS		= 256;
	public static final int	PARAMETERS_END	= 512;
	public static final int	VARIABLE		= 1024;
	
	public static final int	BYTECODE		= 2048;
	public static final int	BYTECODE_2		= 4096;
	
	protected IContext		context;
	protected IValued		field;
	protected int			precedence;
	
	private IValue			value;
	
	private boolean			dotless;
	
	public ExpressionParser(IContext context, IValued field)
	{
		this.mode = VALUE;
		this.context = context;
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0 || ";".equals(value))
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
				this.value = new ThisValue(token.raw(), this.context.getThisType());
				return true;
			}
			else if ("super".equals(value))
			{
				this.mode = ACCESS;
				this.value = new SuperValue(token.raw(), this.context.getThisType());
				return true;
			}
			else if ("return".equals(value))
			{
				ReturnStatement statement = new ReturnStatement(token.raw());
				this.value = statement;
				pm.pushParser(new ExpressionParser(this.context, statement));
				return true;
			}
			else if ("if".equals(value))
			{
				IfStatement statement = new IfStatement(token.raw());
				this.value = statement;
				pm.pushParser(new IfStatementParser(this.context, statement));
				this.mode = 0;
				return true;
			}
			else if ("while".equals(value))
			{
				WhileStatement statement = new WhileStatement(token);
				this.value = statement;
				pm.pushParser(new WhileStatementParser(this.context, statement));
				this.mode = 0;
				return true;
			}
			else if ("@".equals(value))
			{
				this.mode = BYTECODE;
				return true;
			}
			else if ("(".equals(value))
			{
				this.mode = TUPLE_END;
				this.value = new TupleValue(token);
				
				if (!token.next().equals(")"))
				{
					pm.pushParser(new ExpressionListParser(this.context, (IValueList) this.value));
				}
				return true;
			}
			else if ("{".equals(value))
			{
				this.mode = LIST_END;
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
				pm.pushParser(new TypeParser(call));
				return true;
			}
			else if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				this.mode = ACCESS | VARIABLE | LAMBDA;
				pm.pushParser(new TypeParser(this), true);
				return true;
			}
			this.mode = ACCESS;
		}
		if (this.isInMode(LIST_END))
		{
			if ("}".equals(value))
			{
				this.value.expandPosition(token);
				
				if (token.next().equals("."))
				{
					this.mode = ACCESS_2;
					this.dotless = false;
					pm.skip();
					return true;
				}
				
				pm.popParser();
				return true;
			}
			return false;
		}
		if (this.isInMode(TUPLE_END))
		{
			if (")".equals(value))
			{
				this.value.expandPosition(token);
				this.mode = ACCESS | VARIABLE | LAMBDA;
				return true;
			}
			return false;
		}
		if (this.isInMode(BYTECODE))
		{
			if ("{".equals(value))
			{
				Bytecode bc = new Bytecode(token);
				pm.pushParser(new BytecodeParser(this.context, bc));
				this.value = bc;
				this.mode = BYTECODE_2;
				return true;
			}
			return false;
		}
		if (this.isInMode(BYTECODE_2))
		{
			if ("}".equals(value))
			{
				this.value.expandPosition(token);
				pm.popParser();
				return true;
			}
			return false;
		}
		if (this.isInMode(LAMBDA))
		{
			if ("=>".equals(value))
			{
				LambdaValue lv = getLambdaValue(this.value);
				if (lv != null)
				{
					lv.expandPosition(token);
					this.value = lv;
					pm.popParser();
					pm.pushParser(new ExpressionParser(this.context, lv));
					return true;
				}
				return false;
			}
		}
		if (this.isInMode(VARIABLE))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER) && token.next().equals("="))
			{
				ICodePosition pos = token.raw();
				IType type;
				int i = this.value.getValueType();
				if (i == IValue.CLASS_ACCESS)
				{
					type = ((ClassAccess) this.value).type;
				}
				else if (i == IValue.TUPLE)
				{
					type = getTupleType((TupleValue) this.value);
				}
				else
				{
					return false;
				}
				
				FieldAssign access = new FieldAssign(pos, value, null);
				access.field = new Variable(pos, value, type);
				access.initializer = true;
				this.value = access;
				
				pm.skip();
				pm.pushParser(new ExpressionParser(this.context, access));
				
				return true;
			}
		}
		if (this.isInMode(ACCESS))
		{
			if (".".equals(value))
			{
				this.mode = ACCESS_2;
				this.dotless = false;
				return true;
			}
			
			this.dotless = true;
			this.mode = ACCESS_2;
			
			if ("=".equals(value))
			{
				return this.getAssign(pm);
			}
			else if ("(".equals(value))
			{
				IToken prev = token.prev();
				if (prev.isType(IToken.TYPE_IDENTIFIER))
				{
					this.value = new MethodCall(prev, null, prev.value());
					this.mode = PARAMETERS;
				}
				else
				{
					this.value = new MethodCall(this.value.getPosition(), this.value, "apply");
					this.mode = PARAMETERS;
				}
			}
		}
		if (this.isInMode(ACCESS_2))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				if (this.precedence != 0)
				{
					int p = OperatorComparator.index(value);
					if (p != 0 && this.precedence > p)
					{
						pm.popParser(true);
						return true;
					}
				}
				
				return this.getAccess(pm, value, token);
			}
			else if (")".equals(value))
			{
				pm.popParser(true);
				return true;
			}
			else if (token.isType(IToken.TYPE_SYMBOL) || token.isType(IToken.TYPE_CLOSE_BRACKET))
			{
				pm.popParser(true);
				return true;
			}
			
			IToken prev = token.prev();
			if (prev.isType(IToken.TYPE_IDENTIFIER))
			{
				this.value = null;
				pm.reparse();
				return this.getAccess(pm, prev.value(), prev);
			}
			return false;
		}
		if (this.isInMode(PARAMETERS))
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ExpressionListParser(this.context, (IValueList) this.value));
				this.mode = PARAMETERS_END;
				return true;
			}
			return false;
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (")".equals(value))
			{
				this.value.expandPosition(token);
				this.mode = ACCESS;
				return true;
			}
			return false;
		}
		
		if (this.value != null)
		{
			this.value.expandPosition(token);
			pm.popParser(true);
			return true;
		}
		return false;
	}
	
	private boolean getAccess(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		IToken next = token.next();
		if (next.equals("("))
		{
			MethodCall call = new MethodCall(token, this.value, value);
			this.value = call;
			this.mode = PARAMETERS;
			return true;
		}
		else if (!next.isType(IToken.TYPE_IDENTIFIER) && !next.isType(IToken.TYPE_SYMBOL))
		{
			MethodCall call = new MethodCall(token, this.value, value);
			call.setSugar(true);
			call.dotless = this.dotless;
			this.value = call;
			this.mode = ACCESS;
			
			ExpressionParser parser = new ExpressionParser(this.context, this);
			parser.precedence = OperatorComparator.index(value);
			pm.pushParser(parser);
			return true;
		}
		else
		{
			FieldAccess access = new FieldAccess(token, this.value, value);
			access.dotless = this.dotless;
			this.value = access;
			this.mode = ACCESS;
			return true;
		}
	}
	
	private boolean getAssign(ParserManager pm)
	{
		String name = null;
		IValue instance = null;
		int i = this.value.getValueType();
		if (i == IValue.CLASS_ACCESS)
		{
			name = ((ClassAccess) this.value).getName();
		}
		else if (i == IValue.FIELD_ACCESS)
		{
			FieldAccess fa = (FieldAccess) this.value;
			name = fa.getName();
			instance = fa.getValue();
		}
		else if (i == IValue.METHOD_CALL)
		{
			MethodCall call = (MethodCall) this.value;
			IValue v;
			if (call.isName("apply"))
			{
				v = call.getValue();
			}
			else
			{
				v = new FieldAccess(this.value.getPosition(), call.getValue(), call.getName());
			}
			MethodCall updateCall = new MethodCall(this.value.getPosition(), v, "update");
			updateCall.setValues(call.getValues());
			this.value = updateCall;
			pm.pushParser(new ExpressionParser(this.context, this));
			return true;
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
	
	private static TupleType getTupleType(TupleValue value)
	{
		TupleType t = new TupleType();
		for (IValue v : value.getValues())
		{
			ClassAccess ca = (ClassAccess) v;
			t.addType(ca.type);
		}
		return t;
	}
	
	private static LambdaValue getLambdaValue(IValue value)
	{
		List<Parameter> params = new ArrayList();
		
		int i = value.getValueType();
		if (i == IValue.CLASS_ACCESS)
		{
			ClassAccess ca = (ClassAccess) value;
			Parameter param = new Parameter();
			param.setName(ca.type.getName(), ca.type.getQualifiedName());
			params.add(param);
		}
		else if (i == IValue.TUPLE)
		{
			for (IValue v : ((TupleValue) value).getValues())
			{
				if (v.getValueType() != IValue.METHOD_CALL)
				{
					return null;
				}
				MethodCall mc = (MethodCall) v;
				
				v = mc.getValue();
				if (!mc.dotless || v.getValueType() != IValue.CLASS_ACCESS)
				{
					return null;
				}
				
				ClassAccess ca = (ClassAccess) v;
				Parameter param = new Parameter();
				param.setName(mc.name, mc.qualifiedName);
				param.setType(ca.type);
				params.add(param);
			}
		}
		return new LambdaValue(value.getPosition(), params);
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
			this.value = new NullValue(token.raw());
			return true;
		}
		// Boolean
		else if ("true".equals(value))
		{
			this.value = new BooleanValue(token.raw(), true);
			return true;
		}
		else if ("false".equals(value))
		{
			this.value = new BooleanValue(token.raw(), false);
			return true;
		}
		// String
		else if (token.isType(IToken.TYPE_STRING))
		{
			this.value = new StringValue(token.raw(), (String) token.object());
			return true;
		}
		// Char
		else if (token.isType(IToken.TYPE_CHAR))
		{
			this.value = new CharValue(token.raw(), (Character) token.object());
			return true;
		}
		// Int
		else if (token.isType(IToken.TYPE_INT))
		{
			this.value = new IntValue(token.raw(), (Integer) token.object());
			return true;
		}
		else if (token.isType(IToken.TYPE_LONG))
		{
			this.value = new LongValue(token.raw(), (Long) token.object());
			return true;
		}
		// Float
		else if (token.isType(IToken.TYPE_FLOAT))
		{
			this.value = new FloatValue(token.raw(), (Float) token.object());
			return true;
		}
		else if (token.isType(IToken.TYPE_DOUBLE))
		{
			this.value = new DoubleValue(token.raw(), (Double) token.object());
			return true;
		}
		return false;
	}
	
	@Override
	public void setType(IType type)
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
