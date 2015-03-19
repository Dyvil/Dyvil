package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.match.CaseStatement;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.pattern.PatternParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.Operators;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public final class ExpressionParser extends Parser implements ITyped, IValued
{
	public static final int	VALUE				= 0x1;
	public static final int	ARRAY_END			= 0x2;
	public static final int	TUPLE_END			= 0x4;
	
	public static final int	ACCESS				= 0x8;
	public static final int	ACCESS_2			= 0x10;
	
	public static final int	LAMBDA				= 0x20;
	public static final int	STATEMENT			= 0x40;
	public static final int	TYPE				= 0x80;
	public static final int	CONSTRUCTOR			= 0x100;
	public static final int	PARAMETERS			= 0x200;
	public static final int	PARAMETERS_END		= 0x400;
	public static final int	GENERICS			= 0x800;
	public static final int	GENERICS_END		= 0x1000;
	public static final int	VARIABLE			= 0x2000;
	
	public static final int	FUNCTION_POINTER	= 0x4000;
	
	public static final int	BYTECODE_END		= 0x10000;
	
	public static final int	PATTERN_IF			= 0x20000;
	public static final int	PATTERN_END			= 0x40000;
	
	protected IValued		field;
	protected int			precedence;
	
	private IValue			value;
	
	private boolean			dotless;
	private boolean			prefix;
	
	public ExpressionParser(IValued field)
	{
		this.mode = VALUE;
		this.field = field;
	}
	
	@Override
	public void reset()
	{
		this.mode = VALUE;
		this.value = null;
		this.dotless = false;
		this.prefix = false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0 || type == Tokens.SEMICOLON || type == Tokens.COMMA)
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		
		if (this.isInMode(VALUE))
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				this.mode = TUPLE_END;
				TupleValue tv = new TupleValue(token);
				this.value = tv;
				
				int nextType = token.next().type();
				if (nextType != Tokens.CLOSE_PARENTHESIS)
				{
					pm.pushParser(new ExpressionListParser(tv));
				}
				return;
			}
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				this.mode = ACCESS | VARIABLE | LAMBDA;
				pm.pushParser(new TypeParser(this), true);
				return;
			}
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				this.mode = ARRAY_END;
				StatementList sl = new StatementList(token);
				this.value = sl;
				
				int nextType = token.next().type();
				if (nextType != Tokens.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new ExpressionListParser(sl));
				}
				return;
			}
			if ((type & Tokens.SYMBOL_IDENTIFIER) == Tokens.SYMBOL_IDENTIFIER)
			{
				if (token.equals("@") && token.next().type() == Tokens.OPEN_CURLY_BRACKET)
				{
					Bytecode bc = new Bytecode(token);
					pm.skip();
					pm.pushParser(new BytecodeParser(bc));
					this.mode = BYTECODE_END;
					this.value = bc;
					return;
				}
				
				this.prefix = true;
				this.getAccess(pm, token.text(), token, type);
				return;
			}
			if (type == Tokens.ARROW_OPERATOR)
			{
				this.mode = ACCESS | VARIABLE;
				pm.pushParser(new TypeParser(this), true);
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				this.mode = ACCESS | VARIABLE | LAMBDA;
				pm.pushParser(new TypeParser(this), true);
				return;
			}
			if (this.parsePrimitive(token, type))
			{
				this.mode = ACCESS;
				return;
			}
			if (this.parseKeyword(pm, token, type))
			{
				return;
			}
			
			this.mode = ACCESS;
		}
		if (this.isInMode(PATTERN_IF))
		{
			this.mode = PATTERN_END;
			if (type == Tokens.IF)
			{
				pm.pushParser(new ExpressionParser(this));
				return;
			}
		}
		if (this.isInMode(PATTERN_END))
		{
			this.field.setValue(this.value);
			pm.popParser();
			pm.pushParser(new ExpressionParser((IValued) this.value));
			if (type == Tokens.COLON)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Pattern - ':' expected");
		}
		if (type == Tokens.COLON)
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		if (this.isInMode(ARRAY_END))
		{
			this.field.setValue(this.value);
			pm.popParser();
			this.value.expandPosition(token);
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				if (token.next().equals("."))
				{
					this.mode = ACCESS_2;
					this.dotless = false;
					pm.skip();
					return;
				}
				return;
			}
			throw new SyntaxError(token, "Invalid Array - '}' expected");
		}
		if (this.isInMode(TUPLE_END))
		{
			this.mode = ACCESS | VARIABLE | LAMBDA;
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple - ')' expected", true);
		}
		if (this.isInMode(PARAMETERS))
		{
			this.mode = PARAMETERS_END;
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				ICall call = (ICall) this.value;
				call.setArguments(this.getArguments(pm, token.next()));
				return;
			}
			throw new SyntaxError(token, "Invalid Argument List - '(' expected", true);
		}
		if (this.isInMode(PARAMETERS_END))
		{
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Argument List - ')' expected", true);
		}
		if (this.isInMode(GENERICS))
		{
			this.mode = GENERICS_END;
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser((ITypeList) this.value));
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Arguments - '[' expected");
		}
		if (this.isInMode(GENERICS_END))
		{
			this.mode = ACCESS;
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Arguments - ']' expected");
		}
		if (this.isInMode(FUNCTION_POINTER))
		{
			// TODO Constructor Function Pointers
			pm.popParser();
			if (ParserUtil.isIdentifier(type))
			{
				FunctionValue fl = new FunctionValue(token.raw(), token.text());
				fl.instance = this.value;
				this.field.setValue(fl);
				return;
			}
			throw new SyntaxError(token, "Invalid Function Pointer - Identifier expected");
		}
		if (this.isInMode(BYTECODE_END))
		{
			this.field.setValue(this.value);
			pm.popParser();
			this.value.expandPosition(token);
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Bytecode Expression - '}' expected", true);
		}
		if (ParserUtil.isCloseBracket(type))
		{
			this.field.setValue(this.value);
			pm.popParser(true);
			return;
		}
		if (this.isInMode(LAMBDA))
		{
			if (type == Tokens.ARROW_OPERATOR)
			{
				LambdaValue lv = getLambdaValue(this.value);
				if (lv != null)
				{
					lv.expandPosition(token);
					this.field.setValue(lv);
					pm.popParser();
					pm.pushParser(new ExpressionParser(lv));
					return;
				}
				
				if (this.value.getValueType() == IValue.TUPLE)
				{
					TupleType tt = getTupleType((TupleValue) this.value);
					if (tt != null)
					{
						LambdaType lt = new LambdaType(tt);
						pm.pushParser(new TypeParser(lt));
						this.value = new ClassAccess(null, lt);
						this.mode = VARIABLE;
						return;
					}
				}
				
				throw new SyntaxError(token, "Invalid Lambda Expression");
			}
		}
		if (this.isInMode(VARIABLE))
		{
			if (ParserUtil.isIdentifier(type) && token.next().type() == Tokens.EQUALS)
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
					throw new SyntaxError(token, "Invalid Assignment");
				}
				
				String name = token.text();
				FieldInitializer access = new FieldInitializer(pos, name, itype);
				this.value = access;
				
				pm.skip();
				pm.pushParser(new ExpressionParser(access));
				
				return;
			}
		}
		if (this.isInMode(ACCESS))
		{
			if (type == Tokens.DOT)
			{
				this.mode = ACCESS_2;
				this.dotless = false;
				return;
			}
			if (type == Tokens.HASH)
			{
				this.mode = FUNCTION_POINTER;
				return;
			}
			
			this.dotless = true;
			this.mode = ACCESS_2;
			
			if (type == Tokens.ELSE)
			{
				this.field.setValue(this.value);
				pm.popParser(true);
				return;
			}
			if (type == Tokens.EQUALS)
			{
				this.getAssign(pm, token);
				return;
			}
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				IToken prev = token.prev();
				IToken next = token.next();
				IArguments args;
				args = this.getArguments(pm, next);
				
				int prevType = prev.type();
				if (ParserUtil.isIdentifier(prevType))
				{
					MethodCall mc = new MethodCall(prev, null, prev.text());
					mc.arguments = args;
					this.value = mc;
				}
				else if (prevType == Tokens.CLOSE_SQUARE_BRACKET)
				{
					MethodCall mc;
					if (this.value.getValueType() == IValue.CLASS_ACCESS)
					{
						mc = ((ClassAccess) this.value).toMethodCall();
					}
					else
					{
						mc = (MethodCall) this.value;
					}
					mc.arguments = args;
					this.value = mc;
				}
				else
				{
					ApplyMethodCall amc = new ApplyMethodCall(this.value.getPosition(), this.value);
					amc.arguments = args;
					this.value = amc;
				}
				this.mode = PARAMETERS_END;
				return;
			}
		}
		if (this.isInMode(ACCESS_2))
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.prefix = false;
				String name = token.text();
				if (this.precedence != 0 && this.dotless)
				{
					int p = Operators.index(name);
					if (this.precedence >= p)
					{
						this.field.setValue(this.value);
						pm.popParser(true);
						return;
					}
				}
				
				this.getAccess(pm, name, token, type);
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				this.field.setValue(this.value);
				pm.popParser(true);
				return;
			}
			
			IToken prev = token.prev();
			if (ParserUtil.isIdentifier(prev.type()))
			{
				this.value = null;
				pm.reparse();
				this.getAccess(pm, prev.text(), prev, type);
				return;
			}
			
			ApplyMethodCall call = new ApplyMethodCall(token.raw(), this.value);
			SingleArgument sa = new SingleArgument();
			call.arguments = sa;
			this.value = call;
			this.mode = 0;
			pm.pushParser(new ExpressionParser(sa), true);
			return;
		}
		if (this.isInMode(CONSTRUCTOR))
		{
			ConstructorCall cc = (ConstructorCall) this.value;
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				ArgumentList list = new ArgumentList();
				cc.arguments = list;
				pm.pushParser(new ExpressionListParser(list));
				this.mode = PARAMETERS_END;
				return;
			}
			
			SingleArgument sa = new SingleArgument();
			cc.arguments = sa;
			pm.pushParser(new ExpressionParser(sa), true);
			this.mode = 0;
			return;
		}
		
		if (this.value != null)
		{
			this.value.expandPosition(token);
			this.field.setValue(this.value);
			pm.popParser(true);
			return;
		}
		throw new SyntaxError(token, "Invalid Expression - Invalid Token '" + token.text() + "'");
	}
	
	private IArguments getArguments(IParserManager pm, IToken next) throws SyntaxError
	{
		int type = next.type();
		if (type == Tokens.CLOSE_PARENTHESIS)
		{
			return EmptyArguments.VISIBLE;
		}
		if (ParserUtil.isIdentifier(type) && next.next().type() == Tokens.COLON)
		{
			ArgumentMap map = new ArgumentMap();
			pm.pushParser(new ExpressionMapParser(map));
			return map;
		}
		
		ArgumentList list = new ArgumentList();
		pm.pushParser(new ExpressionListParser(list));
		return list;
	}
	
	private void getAccess(IParserManager pm, String value, IToken token, int type) throws SyntaxError
	{
		IToken next = token.next();
		int type1 = next.type();
		if (type1 == Tokens.OPEN_PARENTHESIS)
		{
			MethodCall call = new MethodCall(token, this.value, value);
			call.dotless = this.dotless;
			this.value = call;
			this.mode = PARAMETERS;
			return;
		}
		if (type1 == Tokens.OPEN_SQUARE_BRACKET)
		{
			MethodCall call = new MethodCall(token, this.value, value);
			call.dotless = this.dotless;
			this.value = call;
			this.mode = GENERICS;
			return;
		}
		if (type == Tokens.SYMBOL_IDENTIFIER || !ParserUtil.isIdentifier(type1) && !ParserUtil.isTerminator2(type1))
		{
			MethodCall call = new MethodCall(token, this.value, value);
			SingleArgument sa = new SingleArgument();
			call.arguments = sa;
			call.dotless = this.dotless;
			this.value = call;
			this.mode = ACCESS;
			
			ExpressionParser parser = new ExpressionParser(sa);
			parser.precedence = this.prefix ? Operators.PREFIX : Operators.index(value);
			pm.pushParser(parser);
			return;
		}
		
		FieldAccess access = new FieldAccess(token, this.value, value);
		access.dotless = this.dotless;
		this.value = access;
		this.mode = ACCESS;
		return;
	}
	
	private void getAssign(IParserManager pm, IToken token) throws SyntaxError
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
			return;
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
			return;
		}
		else if (i == IValue.APPLY_METHOD_CALL)
		{
			ApplyMethodCall call = (ApplyMethodCall) this.value;
			
			UpdateMethodCall updateCall = new UpdateMethodCall(position);
			updateCall.instance = call.instance;
			updateCall.arguments = call.arguments;
			
			this.value = updateCall;
			pm.pushParser(new ExpressionParser(updateCall));
			return;
		}
		else if (i == IValue.METHOD_CALL)
		{
			MethodCall call = (MethodCall) this.value;
			FieldAccess fa = new FieldAccess(position);
			fa.instance = call.instance;
			fa.name = call.name;
			fa.qualifiedName = call.qualifiedName;
			
			UpdateMethodCall updateCall = new UpdateMethodCall(position);
			updateCall.arguments = call.arguments;
			updateCall.instance = fa;
			
			this.value = updateCall;
			pm.pushParser(new ExpressionParser(updateCall));
			return;
		}
		
		throw new SyntaxError(token, "Invalid Assignment");
	}
	
	private static TupleType getTupleType(TupleValue value)
	{
		TupleType t = new TupleType();
		for (IValue v : value)
		{
			ClassAccess ca = (ClassAccess) v;
			t.addType(ca.type);
		}
		return t;
	}
	
	private static LambdaValue getLambdaValue(IValue value)
	{
		int type = value.getValueType();
		if (type == IValue.CLASS_ACCESS)
		{
			ClassAccess ca = (ClassAccess) value;
			LambdaParameter param = new LambdaParameter();
			param.setName(ca.type.getName(), ca.type.getQualifiedName());
			return new LambdaValue(ca.getPosition(), param);
		}
		
		if (type != IValue.TUPLE)
		{
			return null;
		}
		
		TupleValue tv = (TupleValue) value;
		int len = tv.valueCount();
		LambdaParameter[] params = new LambdaParameter[len];
		
		for (int i = 0; i < len; i++)
		{
			IValue v = tv.getValue(i);
			type = v.getValueType();
			if (type == IValue.FIELD_ACCESS)
			{
				FieldAccess fa = (FieldAccess) v;
				if (!fa.dotless)
				{
					return null;
				}
				
				if (fa.instance.getValueType() != IValue.CLASS_ACCESS)
				{
					return null;
				}
				
				LambdaParameter param = new LambdaParameter();
				param.setName(fa.name, fa.qualifiedName);
				param.setType(((ClassAccess) fa.instance).type);
				params[i] = param;
				continue;
			}
			
			return null;
		}
		
		return new LambdaValue(tv.position, params);
	}
	
	public void end()
	{
		if (this.value != null)
		{
			this.field.setValue(this.value);
		}
	}
	
	public boolean parsePrimitive(IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.STRING:
			this.value = new StringValue(token.raw(), token.stringValue());
			return true;
		case Tokens.CHAR:
			this.value = new CharValue(token.raw(), token.charValue());
			return true;
		case Tokens.INT:
			this.value = new IntValue(token.raw(), token.intValue());
			return true;
		case Tokens.LONG:
			this.value = new LongValue(token.raw(), token.longValue());
			return true;
		case Tokens.FLOAT:
			this.value = new FloatValue(token.raw(), token.floatValue());
			return true;
		case Tokens.DOUBLE:
			this.value = new DoubleValue(token.raw(), token.doubleValue());
			return true;
		}
		return false;
	}
	
	public boolean parseKeyword(IParserManager pm, IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.WILDCARD:
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
		{
			ConstructorCall call = new ConstructorCall(token);
			this.mode = CONSTRUCTOR;
			this.value = call;
			pm.pushParser(new TypeParser(this));
			return true;
		}
		case Tokens.RETURN:
		{
			ReturnStatement rs = new ReturnStatement(token.raw());
			this.value = rs;
			pm.pushParser(new ExpressionParser(rs));
			return true;
		}
		case Tokens.IF:
		{
			IfStatement is = new IfStatement(token.raw());
			this.value = is;
			pm.pushParser(new IfStatementParser(is));
			this.mode = 0;
			return true;
		}
		case Tokens.ELSE:
		{
			if (!(this.parent instanceof IfStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'else' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Tokens.WHILE:
		{
			WhileStatement statement = new WhileStatement(token);
			this.value = statement;
			pm.pushParser(new WhileStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Tokens.DO:
		{
			DoStatement statement = new DoStatement(token);
			this.value = statement;
			pm.pushParser(new DoStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Tokens.FOR:
		{
			ForStatement statement = new ForStatement(token);
			this.value = statement;
			pm.pushParser(new ForStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Tokens.BREAK:
		{
			BreakStatement statement = new BreakStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.text());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Tokens.CONTINUE:
		{
			ContinueStatement statement = new ContinueStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.text());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Tokens.GOTO:
		{
			GoToStatement statement = new GoToStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.text());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Tokens.CASE:
		{
			CaseStatement pattern = new CaseStatement(token.raw());
			pm.pushParser(new PatternParser(pattern));
			this.mode = PATTERN_IF;
			this.value = pattern;
			return true;
		}
		case Tokens.TRY:
		{
			TryStatement statement = new TryStatement(token.raw());
			pm.pushParser(new TryStatementParser(statement));
			this.mode = 0;
			this.value = statement;
			return true;
		}
		case Tokens.CATCH:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'catch' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Tokens.FINALLY:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'finally' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Tokens.THROW:
		{
			ThrowStatement statement = new ThrowStatement(token.raw());
			pm.pushParser(new ExpressionParser(statement));
			this.mode = 0;
			this.value = statement;
			return true;
		}
		case Tokens.SYNCHRONIZED: // TODO Synchronized Blocks
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.value == null)
		{
			this.value = new ClassAccess(type.getPosition(), type);
		}
		else
		{
			((ITyped) this.value).setType(type);
		}
	}
	
	@Override
	public Type getType()
	{
		return null;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == PATTERN_END)
		{
			((CaseStatement) this.value).setCondition(value);
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
