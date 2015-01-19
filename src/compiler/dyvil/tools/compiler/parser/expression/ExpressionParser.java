package dyvil.tools.compiler.parser.expression;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.statement.IfStatementParser;
import dyvil.tools.compiler.parser.statement.WhileStatementParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.OperatorComparator;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

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
	public static final int	CONSTRUCTOR		= 256;
	public static final int	PARAMETERS		= 512;
	public static final int	PARAMETERS_END	= 1024;
	public static final int	VARIABLE		= 2048;
	
	public static final int	BYTECODE		= 4096;
	public static final int	BYTECODE_END	= 8192;
	
	protected IValued		field;
	protected int			precedence;
	
	private IValue			value;
	
	private boolean			dotless;
	
	public ExpressionParser(IValued field)
	{
		this.mode = VALUE;
		this.field = field;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0 || type == Tokens.SEMICOLON)
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.isInMode(VALUE))
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				this.mode = TUPLE_END;
				this.value = new TupleValue(token);
				
				if (!token.next().equals(")"))
				{
					pm.pushParser(new ExpressionListParser((IValueList) this.value));
				}
				return true;
			}
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				this.mode = LIST_END;
				this.value = new StatementList(token);
				
				if (!token.next().equals("}"))
				{
					pm.pushParser(new ExpressionListParser((IValueList) this.value));
				}
				return true;
			}
			if ((type & Tokens.TYPE_IDENTIFIER) != 0)
			{
				this.mode = ACCESS | VARIABLE | LAMBDA;
				pm.pushParser(new TypeParser(this), true);
				return true;
			}
			if (this.parsePrimitive(value, token, type))
			{
				this.mode = ACCESS;
				return true;
			}
			if (this.parseKeyword(pm, token, type))
			{
				return true;
			}
			this.mode = ACCESS;
		}
		if (this.isInMode(LIST_END))
		{
			if (type == Tokens.CLOSE_CURLY_BRACKET)
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
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				this.mode = ACCESS | VARIABLE | LAMBDA;
				return true;
			}
			return false;
		}
		if (this.isInMode(BYTECODE))
		{
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				Bytecode bc = new Bytecode(token);
				pm.pushParser(new BytecodeParser(bc));
				this.value = bc;
				this.mode = BYTECODE_END;
				return true;
			}
			return false;
		}
		if (this.isInMode(BYTECODE_END))
		{
			if (type == Tokens.CLOSE_CURLY_BRACKET)
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
					pm.pushParser(new ExpressionParser(lv));
					return true;
				}
				return false;
			}
		}
		if (this.isInMode(VARIABLE))
		{
			if (ParserUtil.isIdentifier(type) && token.next().equals("="))
			{
				ICodePosition pos = token.raw();
				IType itype;
				int i = this.value.getValueType();
				if (i == IValue.CLASS_ACCESS)
				{
					itype = ((ClassAccess) this.value).type;
				}
				else if (i == IValue.TUPLE)
				{
					itype = getTupleType((TupleValue) this.value);
				}
				else
				{
					return false;
				}
				
				FieldAssign access = new FieldAssign(pos, value, null);
				access.field = new Variable(pos, value, itype);
				access.initializer = true;
				this.value = access;
				
				pm.skip();
				pm.pushParser(new ExpressionParser(access));
				
				return true;
			}
		}
		if (this.isInMode(ACCESS))
		{
			if (type == Tokens.DOT)
			{
				this.mode = ACCESS_2;
				this.dotless = false;
				return true;
			}
			
			this.dotless = true;
			this.mode = ACCESS_2;
			
			if (type == Tokens.ELSE)
			{
				pm.popParser(true);
				return true;
			}
			if ("=".equals(value))
			{
				return this.getAssign(pm);
			}
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				IToken prev = token.prev();
				if (prev.isType(Tokens.TYPE_IDENTIFIER))
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
			if (ParserUtil.isIdentifier(type))
			{
				if (this.precedence != 0 && this.dotless)
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
			else if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return true;
			}
			
			IToken prev = token.prev();
			if (prev.isType(Tokens.TYPE_IDENTIFIER))
			{
				this.value = null;
				pm.reparse();
				return this.getAccess(pm, prev.value(), prev);
			}
			return false;
		}
		if (this.isInMode(CONSTRUCTOR))
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionListParser((IValueList) this.value));
				this.mode = PARAMETERS_END;
				return true;
			}
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				SpecialConstructor pc = new SpecialConstructor(token, (ConstructorCall) this.value);
				pm.pushParser(new ExpressionListParser(pc.list));
				this.value = pc;
				this.mode = LIST_END; // LIST_END because it matches a curly bracket.
				return true;
			}
			return false;
		}
		if (this.isInMode(PARAMETERS))
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionListParser((IValueList) this.value));
				this.mode = PARAMETERS_END;
				return true;
			}
			return false;
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
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
		int type = next.type();
		if (type == Tokens.OPEN_PARENTHESIS)
		{
			MethodCall call = new MethodCall(token, this.value, value);
			call.dotless = this.dotless;
			this.value = call;
			this.mode = PARAMETERS;
			return true;
		}
		else if (!ParserUtil.isIdentifier(type) && !ParserUtil.isTerminator(type))
		{
			MethodCall call = new MethodCall(token, this.value, value);
			call.isSugarCall = true;
			call.dotless = this.dotless;
			this.value = call;
			this.mode = ACCESS;
			
			ExpressionParser parser = new ExpressionParser(this);
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
		ICodePosition position = this.value.getPosition();
		int i = this.value.getValueType();
		if (i == IValue.CLASS_ACCESS)
		{
			ClassAccess ca = (ClassAccess) this.value;
			FieldAssign assign = new FieldAssign(position);
			assign.name = ca.type.getName();
			assign.qualifiedName = ca.type.getQualifiedName();
			
			this.value = assign;
			pm.pushParser(new ExpressionParser(assign));
			return true;
		}
		else if (i == IValue.FIELD_ACCESS)
		{
			FieldAccess fa = (FieldAccess) this.value;
			FieldAssign assign = new FieldAssign(position);
			assign.instance = fa.instance;
			assign.name = fa.name;
			assign.qualifiedName = fa.qualifiedName;
			
			this.value = assign;
			pm.pushParser(new ExpressionParser(assign));
			return true;
		}
		else if (i == IValue.METHOD_CALL)
		{
			MethodCall call = (MethodCall) this.value;
			IValue v;
			if (call.isName("apply"))
			{
				v = call.instance;
			}
			else
			{
				FieldAccess fa = new FieldAccess(position);
				fa.instance = call.instance;
				fa.name = call.name;
				fa.qualifiedName = call.qualifiedName;
				v = fa;
			}
			
			MethodCall updateCall = new MethodCall(position);
			updateCall.arguments = call.arguments;
			updateCall.instance = v;
			updateCall.name = "update";
			updateCall.qualifiedName = "update";
			
			this.value = updateCall;
			pm.pushParser(new ExpressionParser(this));
			return true;
		}
		
		return false;
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
	
	public boolean parsePrimitive(String value, IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.TYPE_STRING:
			this.value = new StringValue(token.raw(), (String) token.object());
			return true;
		case Tokens.TYPE_CHAR:
			this.value = new CharValue(token.raw(), (Character) token.object());
			return true;
		case Tokens.TYPE_INT:
			this.value = new IntValue(token.raw(), (Integer) token.object());
			return true;
		case Tokens.TYPE_LONG:
			this.value = new LongValue(token.raw(), (Long) token.object());
			return true;
		case Tokens.TYPE_FLOAT:
			this.value = new FloatValue(token.raw(), (Float) token.object());
			return true;
		case Tokens.TYPE_DOUBLE:
			this.value = new DoubleValue(token.raw(), (Double) token.object());
			return true;
		}
		return false;
	}
	
	public boolean parseKeyword(ParserManager pm, IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.WILDCARD:
			return true;
		case Tokens.AT:
			this.mode = BYTECODE;
			return true;
		case Tokens.NULL:
			this.value = new NullValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Tokens.TRUE:
			this.value = new BooleanValue(token.raw(), true);
			this.mode = ACCESS;
			return true;
		case Tokens.FALSE:
			this.value = new BooleanValue(token.raw(), false);
			this.mode = ACCESS;
			return true;
		case Tokens.THIS:
			this.value = new ThisValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Tokens.SUPER:
			this.value = new SuperValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Tokens.NEW:
			ConstructorCall call = new ConstructorCall(token);
			this.mode = CONSTRUCTOR;
			this.value = call;
			pm.pushParser(new TypeParser(call));
			return true;
		case Tokens.RETURN:
			ReturnStatement rs = new ReturnStatement(token.raw());
			this.value = rs;
			pm.pushParser(new ExpressionParser(rs));
			return true;
		case Tokens.IF:
			IfStatement is = new IfStatement(token.raw());
			this.value = is;
			pm.pushParser(new IfStatementParser(is));
			this.mode = 0;
			return true;
		case Tokens.ELSE:
			pm.popParser(true);
			return true;
		case Tokens.WHILE:
			WhileStatement statement = new WhileStatement(token);
			this.value = statement;
			pm.pushParser(new WhileStatementParser(statement));
			this.mode = 0;
			return true;
		case Tokens.DO:
			return true;
		case Tokens.FOR:
			return true;
		case Tokens.SWITCH:
			return true;
		case Tokens.CASE:
			return true;
		default:
			return false;
		}
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
