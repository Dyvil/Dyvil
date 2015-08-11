package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.pattern.PatternParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class ExpressionParser extends Parser implements ITypeConsumer, IValueConsumer
{
	public static final int		VALUE		= 0x1;
	public static final int		LIST_END	= 0x2;
	private static final int	ARRAY_END	= 0x4;
	
	public static final int	ACCESS		= 0x10;
	public static final int	ACCESS_2	= 0x20;
	
	public static final int	STATEMENT			= 0x80;
	public static final int	TYPE				= 0x100;
	public static final int	CONSTRUCTOR			= 0x200;
	public static final int	CONSTRUCTOR_END		= 0x400;
	public static final int	PARAMETERS_END		= 0x800;
	public static final int	SUBSCRIPT_END		= 0x1000;
	public static final int	TYPE_ARGUMENTS_END	= 0x2000;
	
	public static final int BYTECODE_END = 0x10000;
	
	public static final int	PATTERN_IF	= 0x20000;
	public static final int	PATTERN_END	= 0x40000;
	
	protected IValueConsumer field;
	
	private IValue value;
	
	private boolean		dotless;
	private Operator	operator;
	private boolean		prefix;
	
	public ExpressionParser(IValueConsumer field)
	{
		this.mode = VALUE;
		this.field = field;
	}
	
	public void reset(IValueConsumer field)
	{
		this.mode = VALUE;
		this.field = field;
		this.value = null;
		this.dotless = false;
		this.operator = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (this.mode == END)
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
		
		switch (this.mode)
		{
		case VALUE:
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				IToken next = token.next();
				if (next.type() == Symbols.CLOSE_PARENTHESIS)
				{
					if (next.next().type() == Symbols.ARROW_OPERATOR)
					{
						LambdaExpression le = new LambdaExpression(next.next().raw());
						this.value = le;
						pm.skip(2);
						pm.pushParser(pm.newExpressionParser(le));
						this.mode = ACCESS;
						return;
					}
					
					this.value = new VoidValue(token.to(token.next()));
					pm.skip();
					this.mode = END;
					return;
				}
				
				pm.pushParser(new LambdaOrTupleParser(this), true);
				this.mode = ACCESS;
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
				this.getAccess(pm, token.nameValue(), token, type);
				return;
			}
			if (type == Symbols.AT)
			{
				if (token.next().type() != Symbols.OPEN_CURLY_BRACKET)
				{
					Bytecode bc = new Bytecode(token);
					pm.skip();
					pm.pushParser(new BytecodeParser(bc));
					this.mode = BYTECODE_END;
					this.value = bc;
					return;
				}
				
				pm.report(new SyntaxError(token, "Invalid Expression - Invalid Symbol '@'"));
				return;
			}
			if (type == Symbols.ARROW_OPERATOR)
			{
				LambdaExpression le = new LambdaExpression(token.raw());
				this.value = le;
				this.mode = ACCESS;
				pm.pushParser(pm.newExpressionParser(le));
				return;
			}
			if (type == Symbols.COLON)
			{
				pm.report(new SyntaxError(token, "Invalid Colon - Delete this token"));
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
			pm.reparse();
			return;
		case PATTERN_IF:
			this.mode = PATTERN_END;
			if (type == Keywords.IF)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
		case PATTERN_END:
			if (type == Symbols.COLON)
			{
				this.mode = END;
				if (token.next().type() != Keywords.CASE)
				{
					pm.pushParser(pm.newExpressionParser((IValued) this.value));
				}
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Pattern - ':' expected"));
			return;
		case ARRAY_END:
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				this.mode = ACCESS;
				return;
			}
			this.field.setValue(this.value);
			pm.popParser();
			pm.report(new SyntaxError(token, "Invalid Array - ']' expected"));
			return;
		case LIST_END:
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
			pm.report(new SyntaxError(token, "Invalid Statement List - '}' expected"));
			return;
		case PARAMETERS_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Argument List - ')' expected"));
			return;
		case SUBSCRIPT_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Subscript Arguments - ']' expected"));
			return;
		case CONSTRUCTOR:
		{
			ConstructorCall cc = (ConstructorCall) this.value;
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.createBody(cc.toClassConstructor(), pm);
				return;
			}
			
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				ArgumentList list = new ArgumentList();
				cc.setArguments(list);
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
			cc.setArguments(sa);
			pm.pushParser(pm.newExpressionParser(sa), true);
			this.mode = END;
			return;
		}
		case CONSTRUCTOR_END:
			this.mode = ACCESS;
			if (token.next().type() == Symbols.OPEN_CURLY_BRACKET)
			{
				this.createBody(((ConstructorCall) this.value).toClassConstructor(), pm);
				return;
			}
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Constructor Argument List - ')' expected"));
			return;
		case BYTECODE_END:
			this.field.setValue(this.value);
			pm.popParser();
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Bytecode Expression - '}' expected"));
			return;
		case TYPE_ARGUMENTS_END:
			MethodCall mc = (MethodCall) this.value;
			IToken next = token.next();
			if (next.type() == Symbols.OPEN_PARENTHESIS)
			{
				pm.skip();
				mc.setArguments(this.getArguments(pm, next.next()));
			}
			
			this.mode = ACCESS;
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Method Type Parameter List - ']' expected"));
			return;
		}
		
		if (type == Symbols.COLON)
		{
			this.mode = ACCESS;
			pm.report(new SyntaxError(token, "Invalid Colon - Delete this token"));
			return;
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
		
		if (this.mode == ACCESS)
		{
			if (type == Symbols.DOT)
			{
				this.mode = ACCESS_2;
				this.dotless = false;
				return;
			}
			
			this.dotless = true;
			
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
				pm.pushParser(pm.newTypeParser(co));
				this.value = co;
				return;
			}
			if (type == Keywords.IS)
			{
				InstanceOfOperator io = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(io));
				this.value = io;
				return;
			}
			if (type == Keywords.MATCH)
			{
				MatchExpression me = new MatchExpression(token.raw(), this.value);
				pm.pushParser(new MatchExpressionParser(me));
				this.value = me;
				return;
			}
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				SubscriptGetter getter = new SubscriptGetter(token, this.value);
				this.value = getter;
				this.mode = SUBSCRIPT_END;
				
				pm.pushParser(new ExpressionListParser(getter.getArguments()));
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
					MethodCall mc = new MethodCall(prev, null, prev.nameValue(), args);
					this.value = mc;
				}
				else if (prevType == Symbols.CLOSE_SQUARE_BRACKET)
				{
					AbstractCall mc;
					if (this.value.valueTag() == IValue.FIELD_ACCESS)
					{
						mc = ((FieldAccess) this.value).toMethodCall(null);
					}
					else
					{
						mc = (AbstractCall) this.value;
					}
					mc.setArguments(args);
					this.value = mc;
				}
				else
				{
					ApplyMethodCall amc = new ApplyMethodCall(this.value.getPosition(), this.value, args);
					this.value = amc;
				}
				this.mode = PARAMETERS_END;
				return;
			}
			
			this.mode = ACCESS_2;
		}
		if (this.mode == ACCESS_2)
		{
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (this.dotless)
				{
					if (this.prefix)
					{
						this.field.setValue(this.value);
						pm.popParser(true);
						return;
					}
					
					if (this.operator != null)
					{
						Operator operator = pm.getOperator(name);
						int p;
						if (operator == null || (p = this.operator.precedence) > operator.precedence)
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
								pm.report(new SyntaxError(token,
										"Invalid Operator " + name + " - Operator without associativity is not allowed at this location"));
								return;
							case Operator.INFIX_RIGHT:
							}
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
			if (type == Symbols.HASH || type == Symbols.COLON)
			{
				pm.report(new SyntaxError(token, "Invalid Access - Invalid " + token));
				return;
			}
			
			IToken prev = token.prev();
			if (prev != null && ParserUtil.isIdentifier(prev.type()))
			{
				this.value = null;
				pm.reparse();
				this.getAccess(pm, prev.nameValue(), prev, type);
				return;
			}
			
			if (this.value != null)
			{
				SingleArgument sa = new SingleArgument();
				ApplyMethodCall call = new ApplyMethodCall(token.raw(), this.value, sa);
				this.value = call;
				this.mode = END;
				pm.pushParser(pm.newExpressionParser(sa), true);
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Access - Invalid " + token));
			return;
		}
		
		if (this.value != null)
		{
			this.value.expandPosition(token);
			this.field.setValue(this.value);
			pm.popParser(true);
			return;
		}
		pm.report(new SyntaxError(token, "Invalid Expression - Invalid " + token));
		return;
	}
	
	private void createBody(ClassConstructor cc, IParserManager pm)
	{
		IClass iclass = cc.getNestedClass();
		IClassBody body = iclass.getBody();
		pm.pushParser(new ClassBodyParser(iclass, body));
		this.mode = LIST_END;
		this.value = cc;
		return;
	}
	
	private IArguments getArguments(IParserManager pm, IToken next)
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
	
	private void getAccess(IParserManager pm, Name name, IToken token, int type)
	{
		IToken next = token.next();
		int nextType = next.type();
		if (nextType == Symbols.OPEN_PARENTHESIS)
		{
			MethodCall call = new MethodCall(token.raw(), this.value, name);
			call.setDotless(this.dotless);
			this.value = call;
			this.mode = PARAMETERS_END;
			pm.skip();
			call.setArguments(this.getArguments(pm, next.next()));
			return;
		}
		if (nextType == Symbols.OPEN_SQUARE_BRACKET)
		{
			SubscriptGetter getter = new SubscriptGetter(token, new FieldAccess(token.raw(), this.value, name));
			this.value = getter;
			this.mode = SUBSCRIPT_END;
			
			pm.skip();
			pm.pushParser(new ExpressionListParser(getter.getArguments()));
			return;
		}
		if (nextType == Symbols.ARROW_OPERATOR)
		{
			LambdaExpression lv = new LambdaExpression(next.raw(), new MethodParameter(token.raw(), token.nameValue()));
			this.mode = END;
			this.value = lv;
			pm.pushParser(pm.newExpressionParser(lv));
			pm.skip();
			return;
		}
		if (nextType == Symbols.GENERIC_CALL)
		{
			MethodCall mc = new MethodCall(token.raw(), this.value, token.nameValue());
			GenericData gd = new GenericData();
			mc.setGenericData(gd);
			mc.setDotless(this.dotless);
			this.value = mc;
			this.mode = TYPE_ARGUMENTS_END;
			pm.skip();
			pm.pushParser(new TypeListParser(gd));
			return;
		}
		Operator op = pm.getOperator(name);
		if (op != null)
		{
			if (this.value == null || op.type == Operator.PREFIX)
			{
				SingleArgument sa = new SingleArgument();
				MethodCall call = new MethodCall(token, null, name, sa);
				call.setDotless(this.dotless);
				this.value = call;
				this.mode = ACCESS;
				
				ExpressionParser parser = (ExpressionParser) pm.newExpressionParser(sa);
				parser.operator = op;
				parser.prefix = true;
				pm.pushParser(parser);
				return;
			}
			MethodCall call = new MethodCall(token, this.value, name);
			call.setDotless(this.dotless);
			this.value = call;
			this.mode = ACCESS;
			if (op.type != Operator.POSTFIX && !ParserUtil.isTerminator2(nextType))
			{
				SingleArgument sa = new SingleArgument();
				call.setArguments(sa);
				
				ExpressionParser parser = (ExpressionParser) pm.newExpressionParser(sa);
				parser.operator = op;
				pm.pushParser(parser);
			}
			return;
		}
		if (!name.qualified.endsWith("$eq"))
		{
			if (ParserUtil.isTerminator2(nextType))
			{
				FieldAccess access = new FieldAccess(token, this.value, name);
				access.setDotless(this.dotless);
				this.value = access;
				this.mode = ACCESS;
				return;
			}
			if (ParserUtil.isIdentifier(nextType))
			{
				if (ParserUtil.isOperator(pm, next, nextType) || !ParserUtil.isTerminator2(next.next().type()))
				{
					FieldAccess access = new FieldAccess(token, this.value, name);
					access.setDotless(this.dotless);
					this.value = access;
					this.mode = ACCESS;
					return;
				}
			}
		}
		
		SingleArgument sa = new SingleArgument();
		MethodCall call = new MethodCall(token, this.value, name, sa);
		call.setDotless(this.dotless);
		
		this.value = call;
		this.mode = ACCESS;
		ExpressionParser parser = (ExpressionParser) pm.newExpressionParser(sa);
		parser.operator = op;
		pm.pushParser(parser);
		return;
	}
	
	private void getAssign(IParserManager pm, IToken token)
	{
		if (this.value == null)
		{
			this.mode = VALUE;
			pm.report(new SyntaxError(token, "Invalid Assignment - Delete this token"));
			return;
		}
		
		ICodePosition position = this.value.getPosition();
		int valueType = this.value.valueTag();
		switch (valueType)
		{
		case IValue.FIELD_ACCESS:
		{
			FieldAccess fa = (FieldAccess) this.value;
			FieldAssign assign = new FieldAssign(position, fa.getInstance(), fa.getName());
			this.value = assign;
			pm.pushParser(pm.newExpressionParser(assign));
			return;
		}
		case IValue.APPLY_CALL:
		{
			ApplyMethodCall call = (ApplyMethodCall) this.value;
			UpdateMethodCall updateCall = new UpdateMethodCall(position, call.getValue(), call.getArguments());
			this.value = updateCall;
			pm.pushParser(pm.newExpressionParser(updateCall));
			return;
		}
		case IValue.METHOD_CALL:
		{
			MethodCall call = (MethodCall) this.value;
			FieldAccess fa = new FieldAccess(position, call.getValue(), call.getName());
			UpdateMethodCall updateCall = new UpdateMethodCall(position, fa, call.getArguments());
			this.value = updateCall;
			pm.pushParser(pm.newExpressionParser(updateCall));
			return;
		}
		case IValue.SUBSCRIPT_GET:
		{
			SubscriptGetter getter = (SubscriptGetter) this.value;
			SubscriptSetter setter = new SubscriptSetter(position, getter.getValue(), getter.getArguments());
			this.value = setter;
			pm.pushParser(pm.newExpressionParser(setter));
			return;
		}
		}
		
		pm.report(new SyntaxError(token, "Invalid Assignment"));
		return;
	}
	
	private boolean parseKeyword(IParserManager pm, IToken token, int type)
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
			// this[type]
			if (token.next().type() == Symbols.OPEN_SQUARE_BRACKET)
			{
				ThisValue tv = new ThisValue(token.raw());
				this.mode = ARRAY_END;
				this.value = tv;
				pm.skip();
				pm.pushParser(new TypeParser(tv));
				return true;
			}
			this.value = new ThisValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.SUPER:
			if (token.next().type() == Symbols.OPEN_SQUARE_BRACKET)
			{
				SuperValue sv = new SuperValue(token.raw());
				this.mode = ARRAY_END;
				this.value = sv;
				pm.skip();
				pm.pushParser(new TypeParser(sv));
				return true;
			}
			this.value = new SuperValue(token.raw());
			this.mode = ACCESS;
			return true;
		case Keywords.CLASS:
		{
			ClassOperator co = new ClassOperator(token);
			this.value = co;
			pm.pushParser(pm.newTypeParser(co));
			this.mode = ACCESS;
			return true;
		}
		case Keywords.TYPE:
		{
			TypeOperator to = new TypeOperator(token);
			this.value = to;
			pm.pushParser(pm.newTypeParser(to));
			this.mode = ACCESS;
			return true;
		}
		case Keywords.NEW:
		{
			ConstructorCall call = new ConstructorCall(token);
			this.mode = CONSTRUCTOR;
			this.value = call;
			pm.pushParser(pm.newTypeParser(this));
			return true;
		}
		case Keywords.RETURN:
		{
			ReturnStatement rs = new ReturnStatement(token.raw());
			this.value = rs;
			pm.pushParser(pm.newExpressionParser(rs));
			return true;
		}
		case Keywords.IF:
		{
			IfStatement is = new IfStatement(token.raw());
			this.value = is;
			pm.pushParser(new IfStatementParser(is));
			this.mode = END;
			return true;
		}
		case Keywords.ELSE:
		{
			if (!(this.parent instanceof IfStatementParser))
			{
				pm.report(new SyntaxError(token, "Invalid Expression - 'else' not allowed at this location"));
				return true;
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
			this.mode = END;
			return true;
		}
		case Keywords.DO:
		{
			DoStatement statement = new DoStatement(token);
			this.value = statement;
			pm.pushParser(new DoStatementParser(statement));
			this.mode = END;
			return true;
		}
		case Keywords.FOR:
		{
			pm.pushParser(new ForStatementParser(this.field, token.raw()));
			this.mode = END;
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
			this.mode = END;
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
			this.mode = END;
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
			this.mode = END;
			return true;
		}
		case Keywords.CASE:
		{
			CaseExpression pattern = new CaseExpression(token.raw());
			pm.pushParser(new PatternParser(pattern));
			this.mode = PATTERN_IF;
			this.value = pattern;
			return true;
		}
		case Keywords.TRY:
		{
			TryStatement statement = new TryStatement(token.raw());
			pm.pushParser(new TryStatementParser(statement));
			this.mode = END;
			this.value = statement;
			return true;
		}
		case Keywords.CATCH:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				pm.report(new SyntaxError(token, "Invalid Expression - 'catch' not allowed at this location"));
				return true;
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Keywords.FINALLY:
		{
			if (!(this.parent instanceof TryStatementParser))
			{
				pm.report(new SyntaxError(token, "Invalid Expression - 'finally' not allowed at this location"));
				return true;
			}
			this.field.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case Keywords.THROW:
		{
			ThrowStatement statement = new ThrowStatement(token.raw());
			pm.pushParser(pm.newExpressionParser(statement));
			this.mode = END;
			this.value = statement;
			return true;
		}
		case Keywords.SYNCHRONIZED:
		{
			SyncStatement statement = new SyncStatement(token.raw());
			pm.pushParser(new SyncStatementParser(statement));
			this.mode = END;
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
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case PATTERN_END:
			((CaseExpression) this.value).setCondition(value);
			return;
		default:
			this.value = value;
			return;
		}
	}
}
