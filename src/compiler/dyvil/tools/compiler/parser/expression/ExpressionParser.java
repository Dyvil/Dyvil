package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.pattern.PatternParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class ExpressionParser extends Parser implements ITyped, IValued
{
	public static final int		VALUE				= 0x1;
	public static final int		LIST_END			= 0x2;
	private static final int	ARRAY_END			= 0x4;
	public static final int		TUPLE_END			= 0x8;
	
	public static final int		ACCESS				= 0x10;
	public static final int		ACCESS_2			= 0x20;
	
	public static final int		LAMBDA				= 0x40;
	public static final int		STATEMENT			= 0x80;
	public static final int		TYPE				= 0x100;
	public static final int		CONSTRUCTOR			= 0x200;
	public static final int		CONSTRUCTOR_END		= 0x400;
	public static final int		PARAMETERS			= 0x800;
	public static final int		PARAMETERS_END		= 0x1000;
	
	public static final int		FUNCTION_POINTER	= 0x8000;
	
	public static final int		BYTECODE_END		= 0x10000;
	
	public static final int		PATTERN_IF			= 0x20000;
	public static final int		PATTERN_END			= 0x40000;
	
	protected IValued			field;
	
	private IValue				value;
	
	private boolean				dotless;
	private Operator			operator;
	private boolean				prefix;
	
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
		this.operator = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0)
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		switch (type)
		{
		case Symbols.SEMICOLON:
		case Symbols.COMMA:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		
		if (this.mode == VALUE)
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				this.mode = TUPLE_END;
				Tuple tv = new Tuple(token);
				this.value = tv;
				
				int nextType = token.next().type();
				if (nextType != Symbols.CLOSE_PARENTHESIS)
				{
					pm.pushParser(new ExpressionListParser(tv));
				}
				return;
			}
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				this.mode = ARRAY_END;
				Array vl = new Array(token);
				this.value = vl;
				
				int nextType = token.next().type();
				if (nextType != Symbols.CLOSE_SQUARE_BRACKET)
				{
					pm.pushParser(new ExpressionListParser(vl));
				}
				return;
			}
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.mode = LIST_END;
				StatementList sl = new StatementList(token);
				this.value = sl;
				
				int nextType = token.next().type();
				if (nextType != Symbols.CLOSE_CURLY_BRACKET)
				{
					pm.pushParser(new StatementListParser(sl));
				}
				return;
			}
			if (type == Tokens.SYMBOL_IDENTIFIER)
			{
				if (token.nameValue() == Name.at && token.next().type() == Symbols.OPEN_CURLY_BRACKET)
				{
					Bytecode bc = new Bytecode(token);
					pm.skip();
					pm.pushParser(new BytecodeParser(bc));
					this.mode = BYTECODE_END;
					this.value = bc;
					return;
				}
				
				this.getAccess(pm, token.nameValue(), token, type);
				return;
			}
			if ((type & Tokens.IDENTIFIER) != 0)
			{
				this.getAccess(pm, token.nameValue(), token, type);
				return;
			}
			if (this.parseKeyword(pm, token, type))
			{
				return;
			}
			
			this.mode = ACCESS;
		}
		if (this.mode == PATTERN_IF)
		{
			this.mode = PATTERN_END;
			if (type == Keywords.IF)
			{
				pm.pushParser(new ExpressionParser(this));
				return;
			}
		}
		if (this.mode == PATTERN_END)
		{
			if (type == Symbols.COLON)
			{
				this.field.setValue(this.value);
				pm.popParser();
				if (token.next().type() != Keywords.CASE)
				{
					pm.pushParser(new ExpressionParser((IValued) this.value));
				}
				return;
			}
			throw new SyntaxError(token, "Invalid Pattern - ':' expected");
		}
		if (type == Symbols.COLON)
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		if (this.mode == ARRAY_END)
		{
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				this.mode = ACCESS;
				return;
			}
			this.field.setValue(this.value);
			pm.popParser();
			throw new SyntaxError(token, "Invalid Array - ']' expected");
		}
		if (this.mode == LIST_END)
		{
			this.field.setValue(this.value);
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				if (token.next().type() == Symbols.DOT)
				{
					this.mode = ACCESS_2;
					this.dotless = false;
					pm.skip();
					return;
				}
				pm.popParser();
				return;
			}
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Statement List - '}' expected");
		}
		if (this.mode == TUPLE_END)
		{
			this.mode = ACCESS | LAMBDA;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple - ')' expected", true);
		}
		if (this.mode == PARAMETERS)
		{
			this.mode = PARAMETERS_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				ICall call = (ICall) this.value;
				call.setArguments(this.getArguments(pm, token.next()));
				return;
			}
			throw new SyntaxError(token, "Invalid Argument List - '(' expected", true);
		}
		if (this.mode == PARAMETERS_END)
		{
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Argument List - ')' expected", true);
		}
		if (this.mode == CONSTRUCTOR_END)
		{
			this.mode = ACCESS;
			if (token.next().type() == Symbols.OPEN_CURLY_BRACKET)
			{
				ClassConstructor cc = ((ConstructorCall) this.value).toClassConstructor();
				pm.skip();
				pm.pushParser(new ClassBodyParser(cc.getNestedClass()));
				this.value = cc;
				this.mode = LIST_END;
				return;
			}
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Constructor Argument List - ')' expected", true);
		}
		if (this.mode == FUNCTION_POINTER)
		{
			// TODO Constructor Function Pointers
			pm.popParser();
			if (ParserUtil.isIdentifier(type))
			{
				FunctionPointer fl = new FunctionPointer(token.raw(), token.nameValue());
				fl.instance = this.value;
				this.field.setValue(fl);
				return;
			}
			throw new SyntaxError(token, "Invalid Function Pointer - Identifier expected");
		}
		if (this.mode == BYTECODE_END)
		{
			this.field.setValue(this.value);
			pm.popParser();
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Bytecode Expression - '}' expected", true);
		}
		if (ParserUtil.isCloseBracket(type))
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		if (this.isInMode(LAMBDA))
		{
			if (type == Symbols.ARROW_OPERATOR)
			{
				LambdaExpression lv = getLambdaValue(this.value);
				if (lv != null)
				{
					lv.expandPosition(token);
					this.field.setValue(lv);
					pm.popParser();
					pm.pushParser(new ExpressionParser(lv));
					return;
				}
				
				throw new SyntaxError(token, "Invalid Lambda Expression");
			}
		}
		if (this.isInMode(ACCESS))
		{
			if (type == Symbols.DOT)
			{
				this.mode = ACCESS_2;
				this.dotless = false;
				return;
			}
			if (type == Symbols.HASH)
			{
				this.mode = FUNCTION_POINTER;
				return;
			}
			
			this.dotless = true;
			this.mode = ACCESS_2;
			
			if (type == Keywords.ELSE)
			{
				this.field.setValue(this.value);
				pm.popParser(true);
				return;
			}
			if (type == Symbols.EQUALS)
			{
				this.getAssign(pm, token);
				return;
			}
			if (type == Keywords.AS)
			{
				CastOperator co = new CastOperator(token.raw(), this.value);
				pm.pushParser(new TypeParser(co));
				this.value = co;
				return;
			}
			if (type == Keywords.IS)
			{
				InstanceOfOperator io = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(new TypeParser(io));
				this.value = io;
				return;
			}
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				IToken prev = token.prev();
				IToken next = token.next();
				IArguments args;
				args = this.getArguments(pm, next);
				
				int prevType = prev.type();
				if (ParserUtil.isIdentifier(prevType))
				{
					MethodCall mc = new MethodCall(prev, null, prev.nameValue());
					mc.arguments = args;
					this.value = mc;
				}
				else if (prevType == Symbols.CLOSE_SQUARE_BRACKET)
				{
					MethodCall mc;
					if (this.value.valueTag() == IValue.FIELD_ACCESS)
					{
						mc = ((FieldAccess) this.value).toMethodCall(null);
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
					ApplyMethodCall amc = new ApplyMethodCall(this.value.getPosition());
					amc.instance = this.value;
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
				Name name = token.nameValue();
				if (this.prefix)
				{
					this.field.setValue(this.value);
					pm.popParser(true);
					return;
				}
				if (this.dotless && this.operator != null)
				{
					Operator operator = pm.getOperator(name);
					int p = this.operator.precedence;
					if (p > operator.precedence)
					{
						this.field.setValue(this.value);
						pm.popParser(true);
						return;
					}
					if (p == operator.precedence)
					{
						switch (operator.type)
						{
						case Operator.INFIX_LEFT:
							this.field.setValue(this.value);
							pm.popParser(true);
							return;
						case Operator.INFIX_NONE:
							throw new SyntaxError(token, "Invalid Operator " + name + " - Operator without associativity is not allowed at this location");
						case Operator.INFIX_RIGHT:
						}
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
				this.getAccess(pm, prev.nameValue(), prev, type);
				return;
			}
			
			if (this.value != null)
			{
				ApplyMethodCall call = new ApplyMethodCall(token.raw());
				call.instance = this.value;
				SingleArgument sa = new SingleArgument();
				call.arguments = sa;
				this.value = call;
				this.mode = 0;
				pm.pushParser(new ExpressionParser(sa), true);
				return;
			}
			throw new SyntaxError(token, "Invalid Access - Invalid " + token);
		}
		if (this.isInMode(CONSTRUCTOR))
		{
			ConstructorCall cc = (ConstructorCall) this.value;
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				ClassConstructor cc2 = cc.toClassConstructor();
				pm.pushParser(new ClassBodyParser(cc2.getNestedClass()));
				this.mode = LIST_END;
				this.value = cc2;
				return;
			}
			
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				ArgumentList list = new ArgumentList();
				cc.arguments = list;
				pm.pushParser(new ExpressionListParser(list));
				this.mode = CONSTRUCTOR_END;
				return;
			}
			
			if (ParserUtil.isTerminator2(type))
			{
				this.mode = ACCESS;
				pm.reparse();
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
		throw new SyntaxError(token, "Invalid Expression - Invalid " + token);
	}
	
	private IArguments getArguments(IParserManager pm, IToken next) throws SyntaxError
	{
		int type = next.type();
		if (type == Symbols.CLOSE_PARENTHESIS)
		{
			return EmptyArguments.VISIBLE;
		}
		if (ParserUtil.isIdentifier(type) && next.next().type() == Symbols.COLON)
		{
			ArgumentMap map = new ArgumentMap();
			pm.pushParser(new ExpressionMapParser(map));
			return map;
		}
		
		ArgumentList list = new ArgumentList();
		pm.pushParser(new ExpressionListParser(list));
		return list;
	}
	
	private void getAccess(IParserManager pm, Name name, IToken token, int type) throws SyntaxError
	{
		IToken next = token.next();
		int type1 = next.type();
		if (type1 == Symbols.OPEN_PARENTHESIS)
		{
			MethodCall call = new MethodCall(token, this.value, name);
			call.dotless = this.dotless;
			this.value = call;
			this.mode = PARAMETERS;
			return;
		}
		if (type1 == Symbols.ARROW_OPERATOR)
		{
			LambdaExpression lv = new LambdaExpression(next.raw(), name);
			this.mode = VALUE;
			this.field.setValue(lv);
			this.field = lv;
			pm.skip();
			return;
		}
		Operator op = pm.getOperator(name);
		if (op != null)
		{
			if (this.value == null || op.type == Operator.PREFIX)
			{
				MethodCall call = new MethodCall(token, null, name);
				SingleArgument sa = new SingleArgument();
				call.arguments = sa;
				call.dotless = this.dotless;
				this.value = call;
				this.mode = ACCESS;
				
				ExpressionParser parser = new ExpressionParser(sa);
				parser.operator = op;
				parser.prefix = true;
				pm.pushParser(parser);
				return;
			}
			MethodCall call = new MethodCall(token, this.value, name);
			this.value = call;
			this.mode = ACCESS;
			call.dotless = this.dotless;
			if (op.type != Operator.POSTFIX)
			{
				SingleArgument sa = new SingleArgument();
				call.arguments = sa;
				
				ExpressionParser parser = new ExpressionParser(sa);
				parser.operator = op;
				pm.pushParser(parser);
			}
			return;
		}
		if (!name.qualified.endsWith("$eq"))
		{
			if (ParserUtil.isTerminator2(type1))
			{
				FieldAccess access = new FieldAccess(token, this.value, name);
				access.dotless = this.dotless;
				this.value = access;
				this.mode = ACCESS;
				return;
			}
			if (ParserUtil.isIdentifier(type1) && !ParserUtil.isTerminator2(next.next().type()))
			{
				FieldAccess access = new FieldAccess(token, this.value, name);
				access.dotless = this.dotless;
				this.value = access;
				this.mode = ACCESS;
				return;
			}
		}
		
		MethodCall call = new MethodCall(token, this.value, name);
		this.value = call;
		this.mode = ACCESS;
		call.dotless = this.dotless;
		
		SingleArgument sa = new SingleArgument();
		call.arguments = sa;
		
		ExpressionParser parser = new ExpressionParser(sa);
		parser.operator = op;
		pm.pushParser(parser);
		return;
	}
	
	private void getAssign(IParserManager pm, IToken token) throws SyntaxError
	{
		ICodePosition position = this.value.getPosition();
		int i = this.value.valueTag();
		if (i == IValue.FIELD_ACCESS)
		{
			FieldAccess fa = (FieldAccess) this.value;
			FieldAssign assign = new FieldAssign(position);
			assign.instance = fa.instance;
			assign.name = fa.name;
			
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
			
			UpdateMethodCall updateCall = new UpdateMethodCall(position);
			updateCall.arguments = call.arguments;
			updateCall.instance = fa;
			
			this.value = updateCall;
			pm.pushParser(new ExpressionParser(updateCall));
			return;
		}
		
		throw new SyntaxError(token, "Invalid Assignment");
	}
	
	private static LambdaExpression getLambdaValue(IValue value)
	{
		int type = value.valueTag();
		if (type != IValue.TUPLE)
		{
			return null;
		}
		
		Tuple tv = (Tuple) value;
		int len = tv.valueCount();
		MethodParameter[] params = new MethodParameter[len];
		
		for (int i = 0; i < len; i++)
		{
			IValue v = tv.getValue(i);
			type = v.valueTag();
			if (type == IValue.FIELD_ACCESS)
			{
				FieldAccess fa = (FieldAccess) v;
				if (!fa.dotless)
				{
					return null;
				}
				
				if (fa.instance.valueTag() != IValue.CLASS_ACCESS)
				{
					return null;
				}
				
				MethodParameter param = new MethodParameter();
				param.name = fa.name;
				param.type = ((ClassAccess) fa.instance).type;
				params[i] = param;
				continue;
			}
			
			return null;
		}
		
		return new LambdaExpression(tv.position, params);
	}
	
	public void end()
	{
		if (this.value != null)
		{
			this.field.setValue(this.value);
		}
	}
	
	public boolean parseKeyword(IParserManager pm, IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Tokens.STRING:
			this.value = new StringValue(token.raw(), token.stringValue());
			this.mode = ACCESS;
			return true;
		case Tokens.STRING_START:
		{
			FormatStringExpression ssv = new FormatStringExpression(token);
			this.value = ssv;
			this.mode = ACCESS;
			pm.pushParser(new FormatStringParser(ssv), true);
			return true;
		}
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
		{
			pm.popParser(true);
			return true;
		}
		case Tokens.CHAR:
			this.value = new CharValue(token.raw(), token.charValue());
			this.mode = ACCESS;
			return true;
		case Tokens.INT:
			this.value = new IntValue(token.raw(), token.intValue());
			this.mode = ACCESS;
			return true;
		case Tokens.LONG:
			this.value = new LongValue(token.raw(), token.longValue());
			this.mode = ACCESS;
			return true;
		case Tokens.FLOAT:
			this.value = new FloatValue(token.raw(), token.floatValue());
			this.mode = ACCESS;
			return true;
		case Tokens.DOUBLE:
			this.value = new DoubleValue(token.raw(), token.doubleValue());
			this.mode = ACCESS;
			return true;
		case Symbols.ELLIPSIS:
			this.value = new WildcardValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Symbols.WILDCARD:
			return true;
		case Keywords.NULL:
			this.value = new NullValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.NIL:
			this.value = new NilValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.TRUE:
			this.value = new BooleanValue(token.raw(), true);
			this.mode = ACCESS;
			return true;
		case Keywords.FALSE:
			this.value = new BooleanValue(token.raw(), false);
			this.mode = ACCESS;
			return true;
		case Keywords.THIS:
			this.value = new ThisValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.SUPER:
			this.value = new SuperValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.CLASS:
		{
			if (token.next().type() != Symbols.OPEN_SQUARE_BRACKET)
			{
				return false;
			}
			
			ClassOperator co = new ClassOperator(token);
			this.value = co;
			pm.skip();
			pm.pushParser(new TypeParser(co));
			this.mode = ARRAY_END;
			return true;
		}
		case Keywords.TYPE:
		{
			if (token.next().type() != Symbols.OPEN_SQUARE_BRACKET)
			{
				return false;
			}
			
			TypeOperator to = new TypeOperator(token);
			this.value = to;
			pm.skip();
			pm.pushParser(new TypeParser(to));
			this.mode = ARRAY_END;
			return true;
		}
		case Keywords.NEW:
		{
			ConstructorCall call = new ConstructorCall(token);
			this.mode = CONSTRUCTOR;
			this.value = call;
			pm.pushParser(new TypeParser(this));
			return true;
		}
		case Keywords.RETURN:
		{
			ReturnStatement rs = new ReturnStatement(token.raw());
			this.value = rs;
			pm.pushParser(new ExpressionParser(rs));
			return true;
		}
		case Keywords.IF:
		{
			IfStatement is = new IfStatement(token.raw());
			this.value = is;
			pm.pushParser(new IfStatementParser(is));
			this.mode = 0;
			return true;
		}
		case Keywords.ELSE:
		{
			if (!(this.parent instanceof IfStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'else' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Keywords.WHILE:
		{
			WhileStatement statement = new WhileStatement(token);
			this.value = statement;
			pm.pushParser(new WhileStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Keywords.DO:
		{
			DoStatement statement = new DoStatement(token);
			this.value = statement;
			pm.pushParser(new DoStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Keywords.FOR:
		{
			ForStatement statement = new ForStatement(token);
			this.value = statement;
			pm.pushParser(new ForStatementParser(statement));
			this.mode = 0;
			return true;
		}
		case Keywords.BREAK:
		{
			BreakStatement statement = new BreakStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.nameValue());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Keywords.CONTINUE:
		{
			ContinueStatement statement = new ContinueStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.nameValue());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Keywords.GOTO:
		{
			GoToStatement statement = new GoToStatement(token);
			this.value = statement;
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.nameValue());
				pm.skip();
			}
			this.mode = 0;
			return true;
		}
		case Keywords.CASE:
		{
			CaseStatement pattern = new CaseStatement(token.raw());
			pm.pushParser(new PatternParser(pattern));
			this.mode = PATTERN_IF;
			this.value = pattern;
			return true;
		}
		case Keywords.TRY:
		{
			TryStatement statement = new TryStatement(token.raw());
			pm.pushParser(new TryStatementParser(statement));
			this.mode = 0;
			this.value = statement;
			return true;
		}
		case Keywords.CATCH:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'catch' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Keywords.FINALLY:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				throw new SyntaxError(token, "Invalid Expression - 'finally' not allowed at this location");
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Keywords.THROW:
		{
			ThrowStatement statement = new ThrowStatement();
			pm.pushParser(new ExpressionParser(statement));
			this.mode = 0;
			this.value = statement;
			return true;
		}
		case Keywords.SYNCHRONIZED:
		{
			SyncStatement statement = new SyncStatement(token.raw());
			pm.pushParser(new SyncStatementParser(statement));
			this.mode = 0;
			this.value = statement;
			return true;
		}
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
