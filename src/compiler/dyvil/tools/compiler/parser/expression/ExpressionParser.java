package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
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
import dyvil.tools.compiler.ast.pattern.ICase;
import dyvil.tools.compiler.ast.statement.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Util;

public final class ExpressionParser extends Parser implements ITypeConsumer, IValueConsumer
{
	public static final int VALUE = 0x1;
	
	public static final int	ACCESS		= 0x2;
	public static final int	ACCESS_2	= 0x4;
	
	public static final int	STATEMENT			= 0x8;
	public static final int	TYPE				= 0x10;
	public static final int	CONSTRUCTOR			= 0x20;
	public static final int	CONSTRUCTOR_END		= 0x40;
	public static final int	ANONYMOUS_CLASS_END	= 0x80;
	public static final int	PARAMETERS			= 0x100;
	public static final int	PARAMETERS_END		= 0x2000;
	public static final int	SUBSCRIPT_END		= 0x4000;
	public static final int	TYPE_ARGUMENTS_END	= 0x8000;
	
	public static final int BYTECODE_END = 0x10000;
	
	public static final int	PATTERN_IF	= 0x20000;
	public static final int	PATTERN_END	= 0x40000;
	
	public static final int	PARAMETERIZED_THIS_END	= 0x80000;
	public static final int	PARAMETERIZED_SUPER_END	= 0x80000;
	
	protected IValueConsumer field;
	
	private IValue value;
	
	private boolean		explicitDot;
	private Operator	operator;
	
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
		this.explicitDot = false;
		this.operator = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		if (this.mode == END)
		{
			if (this.value != null)
			{
				this.field.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		
		int type = token.type();
		switch (type)
		{
		case Symbols.SEMICOLON:
		case Symbols.COLON:
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
			switch (type)
			{
			case Tokens.STRING:
				this.value = new StringValue(token.raw(), token.stringValue());
				this.mode = ACCESS;
				return;
			case Tokens.STRING_START:
			{
				FormatStringExpression ssv = new FormatStringExpression(token);
				this.value = ssv;
				this.mode = ACCESS;
				pm.pushParser(new FormatStringParser(ssv), true);
				return;
			}
			case Tokens.CHAR:
				this.value = new CharValue(token.raw(), token.charValue());
				this.mode = ACCESS;
				return;
			case Tokens.INT:
				this.value = new IntValue(token.raw(), token.intValue());
				this.mode = ACCESS;
				return;
			case Tokens.LONG:
				this.value = new LongValue(token.raw(), token.longValue());
				this.mode = ACCESS;
				return;
			case Tokens.FLOAT:
				this.value = new FloatValue(token.raw(), token.floatValue());
				this.mode = ACCESS;
				return;
			case Tokens.DOUBLE:
				this.value = new DoubleValue(token.raw(), token.doubleValue());
				this.mode = ACCESS;
				return;
			case Symbols.ELLIPSIS:
				this.value = new WildcardValue(token.raw());
				this.mode = ACCESS;
				return;
			case Symbols.WILDCARD:
				return;
			case Symbols.OPEN_PARENTHESIS:
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
			case Symbols.OPEN_SQUARE_BRACKET:
				this.mode = ACCESS;
				pm.pushParser(new ArrayLiteralParser(this), true);
				return;
			case Symbols.OPEN_CURLY_BRACKET:
				this.mode = END;
				pm.pushParser(new StatementListParser(this), true);
				return;
			case Symbols.AT:
				if (token.next().type() == Symbols.OPEN_CURLY_BRACKET)
				{
					Bytecode bc = new Bytecode(token);
					pm.skip();
					pm.pushParser(new BytecodeParser(bc));
					this.mode = BYTECODE_END;
					this.value = bc;
					return;
				}
				Annotation a = new Annotation();
				pm.pushParser(new AnnotationParser(a));
				this.value = new AnnotationValue(a);
				this.mode = 0;
				return;
			case Symbols.ARROW_OPERATOR:
				LambdaExpression le = new LambdaExpression(token.raw());
				this.value = le;
				this.mode = ACCESS;
				pm.pushParser(pm.newExpressionParser(le));
				return;
			}
			if ((type & Tokens.IDENTIFIER) != 0)
			{
				// Identifier at the beginning of an expression
				Name name = token.nameValue();
				this.parseAccess(pm, token, type, name, pm.getOperator(name));
				return;
			}
			if (this.parseKeyword(pm, token, type))
			{
				return;
			}
			
			this.mode = ACCESS;
			// Leave the big switch and jump right over to the ACCESS
			// section
			break;
		case PATTERN_IF:
			this.mode = PATTERN_END;
			if (type == Keywords.IF)
			{
				pm.pushParser(pm.newExpressionParser(v -> ((ICase) this.value).setCondition(v)));
				return;
			}
			//$FALL-THROUGH$
		case PATTERN_END:
			if (type == Symbols.COLON || type == Symbols.ARROW_OPERATOR)
			{
				this.mode = END;
				if (token.next().type() != Keywords.CASE)
				{
					pm.pushParser(pm.newExpressionParser(v -> ((ICase) this.value).setAction(v)));
				}
				return;
			}
			pm.report(token, "Invalid Pattern - ':' expected");
			return;
		case ANONYMOUS_CLASS_END:
			this.value.expandPosition(token);
			this.mode = ACCESS_2;
			
			if (type != Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "Invalid Anonymous Class List - '}' expected");
			}
			
			return;
		case PARAMETERS_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Argument List - ')' expected");
			return;
		case SUBSCRIPT_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Subscript Arguments - ']' expected");
			return;
		case CONSTRUCTOR:
		{
			ConstructorCall cc = (ConstructorCall) this.value;
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.parseBody(pm, cc.toClassConstructor());
				return;
			}
			
			this.mode = PARAMETERS;
			pm.reparse();
			return;
		}
		case PARAMETERS:
		{
			ICall icall = (ICall) this.value;
			
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				IArguments arguments = this.parseArguments(pm, token.next());
				icall.setArguments(arguments);
				this.mode = CONSTRUCTOR_END;
				return;
			}
			
			if (ParserUtil.isExpressionTerminator(type))
			{
				this.mode = ACCESS;
				pm.reparse();
				return;
			}
			
			SingleArgument sa = new SingleArgument();
			icall.setArguments(sa);
			ExpressionParser ep = (ExpressionParser) pm.newExpressionParser(sa);
			ep.operator = Operators.DEFAULT;
			pm.pushParser(ep, true);
			this.mode = END;
			return;
		}
		case CONSTRUCTOR_END:
			this.mode = ACCESS;
			if (token.next().type() == Symbols.OPEN_CURLY_BRACKET)
			{
				pm.skip();
				this.parseBody(pm, ((ConstructorCall) this.value).toClassConstructor());
				return;
			}
			this.value.expandPosition(token);
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Constructor Argument List - ')' expected");
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
			pm.report(token, "Invalid Bytecode Expression - '}' expected");
			return;
		case TYPE_ARGUMENTS_END:
			MethodCall mc = (MethodCall) this.value;
			IToken next = token.next();
			if (next.type() == Symbols.OPEN_PARENTHESIS)
			{
				pm.skip();
				mc.setArguments(this.parseArguments(pm, next.next()));
			}
			
			this.mode = ACCESS;
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			pm.report(token, "Invalid Method Type Parameter List - ']' expected");
			return;
		}
		
		if (ParserUtil.isCloseBracket(type))
		{
			// Close bracket, end expression
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
				this.explicitDot = true;
				return;
			}
			
			this.explicitDot = false;
			
			switch (type)
			{
			case Keywords.ELSE:
				this.field.setValue(this.value);
				pm.popParser(true);
				return;
			case Symbols.EQUALS:
				this.parseAssignment(pm, token);
				return;
			case Keywords.AS:
				CastOperator co = new CastOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(co));
				this.value = co;
				return;
			case Keywords.IS:
				InstanceOfOperator io = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(io));
				this.value = io;
				return;
			case Keywords.MATCH:
				// Parse a match expression
				// e.g. int1 match { ... }, this match { ... }
				MatchExpression me = new MatchExpression(token.raw(), this.value);
				pm.pushParser(new MatchExpressionParser(me));
				this.value = me;
				return;
			case Symbols.OPEN_SQUARE_BRACKET:
				// Parse a subscript getter
				// e.g. this[1], array[0]
				SubscriptGetter getter = new SubscriptGetter(token, this.value);
				this.value = getter;
				this.mode = SUBSCRIPT_END;
				pm.pushParser(new ExpressionListParser(getter.getArguments()));
				return;
			case Symbols.OPEN_PARENTHESIS:
				// Parse an apply call
				// e.g. 1("a"), this("stuff"), "myString"(2)
				ApplyMethodCall amc = new ApplyMethodCall(this.value.getPosition(), this.value, this.parseArguments(pm, token.next()));
				this.value = amc;
				this.mode = PARAMETERS_END;
				return;
			}
			
			this.mode = ACCESS_2;
		}
		if (this.mode == ACCESS_2)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.parseIdentifierAccess(pm, token, type);
				return;
			}
			
			pm.report(token, "Invalid Access - Invalid " + token);
			return;
		}
		
		pm.report(token, "Invalid Expression - Invalid " + token);
		return;
	}
	
	/**
	 * Creates the body and initializes parsing for anonymous classes.
	 * 
	 * @param pm
	 *            the current parsing context manager.
	 * @param cc
	 *            the anonymous class AST node.
	 */
	private void parseBody(IParserManager pm, ClassConstructor cc)
	{
		IClass iclass = cc.getNestedClass();
		IClassBody body = iclass.getBody();
		pm.pushParser(new ClassBodyParser(iclass, body));
		this.mode = ANONYMOUS_CLASS_END;
		this.value = cc;
		return;
	}
	
	/**
	 * Parses an argument list and creates the appropriate AST representation.
	 * The following instances can be created by this method:
	 * <p>
	 * <ul>
	 * <li>{@link EmptyArguments} - For empty argument lists:<br>
	 * <code>
	 * this.call()
	 * </code>
	 * <li>{@link ArgumentList} - For simple indexed argument lists:<br>
	 * <code>
	 * this.call(1, "abc", null)
	 * </code>
	 * <li>{@link ArgumentMap} - For named argument lists / maps:<br>
	 * <code>
	 * this.call(index: 1, string: "abc")
	 * </code>
	 * </ul>
	 * 
	 * @param pm
	 *            the current parsing context manager.
	 * @param next
	 *            the next token. The current token is assumed to be the opening
	 *            parenthesis of the argument list.
	 * @return the appropriate AST representation for the type of argument list.
	 */
	private IArguments parseArguments(IParserManager pm, IToken next)
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
	
	/**
	 * Parses an ACCESS sequence.
	 * 
	 * @param pm
	 *            the current parsing context manager.
	 * @param token
	 *            the current token, has to be any {@code IDENTIFIER} token.
	 * @param type
	 *            the {@code type} of the current {@code token}.
	 * @param name
	 *            the {@code nameValue} of the {@code token}.
	 */
	private void parseAccess(IParserManager pm, IToken token, int type, Name name, Operator op)
	{
		IToken next = token.next();
		int nextType = next.type();
		
		if (op != null && !this.explicitDot)
		{
			if (this.value == null)
			{
				SingleArgument sa = new SingleArgument();
				MethodCall call = new MethodCall(token, null, name, sa);
				call.setDotless(!this.explicitDot);
				this.value = call;
				this.mode = ACCESS;
				
				this.parseApply(pm, token.next(), sa, op);
				return;
			}
			
			MethodCall call = new MethodCall(token, this.value, name);
			call.setDotless(!this.explicitDot);
			this.value = call;
			this.mode = ACCESS;
			if (op.type != Operator.POSTFIX && !ParserUtil.isExpressionTerminator(nextType))
			{
				SingleArgument sa = new SingleArgument();
				call.setArguments(sa);
				
				this.parseApply(pm, token, sa, op);
			}
			return;
		}
		
		switch (nextType)
		{
		case Symbols.OPEN_PARENTHESIS:
			MethodCall call = new MethodCall(token.raw(), this.value, name);
			call.setDotless(!this.explicitDot);
			this.value = call;
			this.mode = PARAMETERS_END;
			pm.skip();
			call.setArguments(this.parseArguments(pm, next.next()));
			return;
		case Symbols.OPEN_SQUARE_BRACKET:
			SubscriptGetter getter = new SubscriptGetter(token, new FieldAccess(token.raw(), this.value, name));
			this.value = getter;
			this.mode = SUBSCRIPT_END;
			pm.skip();
			pm.pushParser(new ExpressionListParser(getter.getArguments()));
			return;
		case Symbols.ARROW_OPERATOR:
			LambdaExpression lv = new LambdaExpression(next.raw(), new MethodParameter(token.raw(), token.nameValue()));
			this.mode = END;
			this.value = lv;
			pm.pushParser(pm.newExpressionParser(lv));
			pm.skip();
			return;
		case Symbols.GENERIC_CALL:
			MethodCall mc = new MethodCall(token.raw(), this.value, token.nameValue());
			GenericData gd = new GenericData();
			mc.setGenericData(gd);
			mc.setDotless(!this.explicitDot);
			this.value = mc;
			this.mode = TYPE_ARGUMENTS_END;
			pm.skip();
			pm.pushParser(new TypeListParser(gd));
			return;
		}
		
		// Name is not a compound operator (does not end with '=')
		if (!name.qualified.endsWith("$eq"))
		{
			// ... TERMINATOR-2
			// e.g. this.someField ;
			if (ParserUtil.isExpressionTerminator(nextType))
			{
				FieldAccess access = new FieldAccess(token, this.value, name);
				access.setDotless(!this.explicitDot);
				this.value = access;
				this.mode = ACCESS;
				return;
			}
			
			// ... IDENTIFIER ...
			// e.g. this call ...
			if (ParserUtil.isIdentifier(nextType))
			{
				// ... OPERATOR ...
				// e.g. this + that
				// ... IDENTIFIER NON-TERMINATOR-2
				// e.g. this.plus that
				if (ParserUtil.isOperator(pm, next, nextType) || !ParserUtil.isExpressionTerminator(next.next().type()))
				{
					FieldAccess access = new FieldAccess(token, this.value, name);
					access.setDotless(!this.explicitDot);
					this.value = access;
					this.mode = ACCESS;
					return;
				}
			}
			
			// else ->
			// e.g. this.call 10;
		}
		else
		{
			// e.g. this += that
			
			op = pm.getOperator(Util.stripEq(name));
		}
		
		SingleArgument sa = new SingleArgument();
		MethodCall call = new MethodCall(token, this.value, name, sa);
		call.setDotless(!this.explicitDot);
		
		this.value = call;
		this.mode = ACCESS;
		
		this.parseApply(pm, token, sa, op == null ? Operators.DEFAULT : op);
		return;
	}
	
	private void parseIdentifierAccess(IParserManager pm, IToken token, int type)
	{
		Name name = token.nameValue();
		Operator operator = pm.getOperator(name);
		if (!this.explicitDot && this.operator != null)
		{
			// Handle operator precedence
			int p;
			if (operator == null || (p = this.operator.precedence) > operator.precedence)
			{
				this.field.setValue(this.value);
				pm.popParser(true);
				return;
			}
			if (p == operator.precedence)
			{
				// Handle associativity
				switch (operator.type)
				{
				case Operator.INFIX_LEFT:
					this.field.setValue(this.value);
					pm.popParser(true);
					return;
				case Operator.INFIX_NONE:
					pm.report(token, "Invalid Operator " + name + " - Operator without associativity is not allowed at this location");
					return;
				case Operator.INFIX_RIGHT:
				}
			}
		}
		
		this.parseAccess(pm, token, type, name, operator);
		return;
	}
	
	/**
	 * Parses an APPLY call, without parenthesis. It might be possible that
	 * {@code pm.reparse()} has to be called after this method, depending on the
	 * token that is passed. E.g.:
	 * <p>
	 * <code>
	 * this 3
	 * print "abc"
	 * button { ... }
	 * </code>
	 * 
	 * @param pm
	 *            the current parsing context manager
	 * @param token
	 *            the first token of the expression that is a parameter to the
	 *            APPLY method
	 * @param sa
	 *            the argument container
	 * @param op
	 *            the operator that precedes this call. Can be null.
	 */
	private void parseApply(IParserManager pm, IToken token, SingleArgument sa, Operator op)
	{
		if (token.type() == Symbols.OPEN_CURLY_BRACKET)
		{
			StatementListParser slp = new StatementListParser(sa);
			slp.setApplied(true);
			pm.pushParser(slp);
			return;
		}
		
		ExpressionParser ep = (ExpressionParser) pm.newExpressionParser(sa);
		ep.operator = op;
		pm.pushParser(ep);
	}
	
	/**
	 * Parses an assignment based on the current {@code value}.
	 * 
	 * @param pm
	 *            the current parsing context manager
	 * @param token
	 *            the current token, i.e. the '=' sign
	 */
	private void parseAssignment(IParserManager pm, IToken token)
	{
		if (this.value == null)
		{
			this.mode = VALUE;
			pm.report(token, "Invalid Assignment - Delete this token");
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
		
		pm.report(token, "Invalid Assignment");
		return;
	}
	
	private boolean parseKeyword(IParserManager pm, IToken token, int type)
	{
		switch (type)
		{
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
		{
			IToken next = token.next();
			switch (next.type())
			{
			// this[type]
			case Symbols.OPEN_SQUARE_BRACKET:
				ThisValue tv = new ThisValue(token.raw());
				this.mode = PARAMETERIZED_THIS_END;
				this.value = tv;
				pm.skip();
				pm.pushParser(new TypeParser(tv));
				return true;
			case Symbols.DOT:
				// this.new
				IToken next2 = next.next();
				if (next2.type() == Keywords.NEW)
				{
					this.value = new InitializerCall(next2.raw(), false);
					pm.skip(2);
					this.mode = PARAMETERS;
					return true;
				}
			}
			this.value = new ThisValue(token.raw());
			this.mode = ACCESS;
			return true;
		}
		case Keywords.SUPER:
		{
			IToken next = token.next();
			switch (next.type())
			{
			// super[type]
			case Symbols.OPEN_SQUARE_BRACKET:
				SuperValue sv = new SuperValue(token.raw());
				this.mode = PARAMETERIZED_SUPER_END;
				this.value = sv;
				pm.skip();
				pm.pushParser(new TypeParser(sv));
				return true;
			case Symbols.DOT:
				// super.new
				IToken next2 = next.next();
				if (next2.type() == Keywords.NEW)
				{
					this.value = new InitializerCall(next2.raw(), true);
					pm.skip(2);
					this.mode = PARAMETERS;
					return true;
				}
			}
			this.value = new SuperValue(token.raw());
			this.mode = ACCESS;
			return true;
		}
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
				pm.report(token, "Invalid Expression - 'else' not allowed at this location");
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
			/*
			 * case Keywords.CASE: { CaseExpression pattern = new
			 * CaseExpression(token.raw()); pm.pushParser(new
			 * PatternParser(pattern)); this.mode = PATTERN_IF; this.value =
			 * pattern; return true; }
			 */
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
				pm.report(token, "Invalid Expression - 'catch' not allowed at this location");
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
				pm.report(token, "Invalid Expression - 'finally' not allowed at this location");
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
		}
		return false;
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
		this.value = value;
	}
}
