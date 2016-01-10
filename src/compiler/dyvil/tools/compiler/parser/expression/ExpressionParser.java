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
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.pattern.ICase;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.statement.ReturnStatement;
import dyvil.tools.compiler.ast.statement.SyncStatement;
import dyvil.tools.compiler.ast.statement.control.BreakStatement;
import dyvil.tools.compiler.ast.statement.control.ContinueStatement;
import dyvil.tools.compiler.ast.statement.control.GoToStatement;
import dyvil.tools.compiler.ast.statement.exception.ThrowStatement;
import dyvil.tools.compiler.ast.statement.exception.TryStatement;
import dyvil.tools.compiler.ast.statement.loop.DoStatement;
import dyvil.tools.compiler.ast.statement.loop.WhileStatement;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.bytecode.BytecodeParser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public final class ExpressionParser extends Parser implements ITypeConsumer, IValueConsumer
{
	public static final int VALUE = 0x1;
	
	public static final int ACCESS     = 0x2;
	public static final int DOT_ACCESS = 0x4;
	
	public static final int STATEMENT              = 0x8;
	public static final int TYPE                   = 0x10;
	public static final int CONSTRUCTOR            = 0x20;
	public static final int CONSTRUCTOR_END        = 0x40;
	public static final int ANONYMOUS_CLASS_END    = 0x80;
	public static final int CONSTRUCTOR_PARAMETERS = 0x100;
	public static final int PARAMETERS_END         = 0x2000;
	public static final int SUBSCRIPT_END          = 0x4000;
	public static final int TYPE_ARGUMENTS_END     = 0x8000;
	
	public static final int BYTECODE_END = 0x10000;
	
	public static final int PATTERN_IF  = 0x20000;
	public static final int PATTERN_END = 0x40000;
	
	public static final int PARAMETERIZED_THIS_END  = 0x80000;
	public static final int PARAMETERIZED_SUPER_END = 0x100000;
	
	protected IValueConsumer valueConsumer;
	
	private IValue value;
	
	private boolean  explicitDot;
	private Operator operator;
	
	public ExpressionParser(IValueConsumer field)
	{
		this.mode = VALUE;
		this.valueConsumer = field;
	}
	
	public void reset(IValueConsumer field)
	{
		this.mode = VALUE;
		this.valueConsumer = field;
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
				this.valueConsumer.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		
		int type = token.type();
		switch (type)
		{
		case Tokens.EOF:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.COMMA:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			if (this.value != null)
			{
				this.valueConsumer.setValue(this.value);
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
				StringInterpolationExpr ssv = new StringInterpolationExpr(token);
				this.value = ssv;
				this.mode = ACCESS;
				pm.pushParser(new StingInterpolationParser(ssv), true);
				return;
			}
			case Tokens.SINGLE_QUOTED_STRING:
				this.value = new CharValue(token.raw(), token.stringValue());
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
			case DyvilSymbols.WILDCARD:
				this.value = new WildcardValue(token.raw());
				this.mode = ACCESS;
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				IToken next = token.next();
				if (next.type() == BaseSymbols.CLOSE_PARENTHESIS)
				{
					// () => ...
					if (next.next().type() == DyvilSymbols.ARROW_OPERATOR)
					{
						LambdaExpr le = new LambdaExpr(next.next().raw());
						this.value = le;
						pm.skip(2);
						pm.pushParser(pm.newExpressionParser(le));
						this.mode = ACCESS;
						return;
					}
					
					// ()
					this.value = new VoidValue(token.to(token.next()));
					pm.skip();
					this.mode = ACCESS;
					return;
				}
				
				// ( ...
				pm.pushParser(new LambdaOrTupleParser(this), true);
				this.mode = ACCESS;
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				this.mode = ACCESS;
				pm.pushParser(new ArrayLiteralParser(this), true);
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = ACCESS;
				pm.pushParser(new StatementListParser(this), true);
				return;
			case DyvilSymbols.AT:
				if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET)
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
				this.mode = END;
				return;
			case DyvilSymbols.ARROW_OPERATOR:
				LambdaExpr le = new LambdaExpr(token.raw());
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
			if (type == DyvilKeywords.IF)
			{
				pm.pushParser(pm.newExpressionParser(((ICase) this.value)::setCondition));
				return;
			}
			//$FALL-THROUGH$
		case PATTERN_END:
			if (type == DyvilSymbols.ARROW_OPERATOR || type == BaseSymbols.COLON)
			{
				this.mode = END;
				if (token.next().type() != DyvilKeywords.CASE)
				{
					pm.pushParser(pm.newExpressionParser(((ICase) this.value)::setAction));
				}
				return;
			}
			pm.report(token, "match.");
			return;
		case ANONYMOUS_CLASS_END:
			this.value.expandPosition(token);
			this.mode = ACCESS;
			
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "class.anonymous.body.end");
			}
			
			return;
		case PARAMETERS_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "method.call.close_paren");
			}
			
			return;
		case SUBSCRIPT_END:
			this.mode = ACCESS;
			this.value.expandPosition(token);
			
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "method.subscript.close_bracket");
			}
			
			return;
		case CONSTRUCTOR:
		{
			ConstructorCall cc = (ConstructorCall) this.value;
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.parseBody(pm, cc.toClassConstructor());
				return;
			}
			
			this.mode = CONSTRUCTOR_PARAMETERS;
			pm.reparse();
			return;
		}
		case CONSTRUCTOR_PARAMETERS:
		{
			ICall icall = (ICall) this.value;
			
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				IArguments arguments = parseArguments(pm, token.next());
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
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "constructor.call.close_paren");
			}
			this.value.expandPosition(token);
			this.mode = ACCESS;
			if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.skip();
				this.parseBody(pm, ((ConstructorCall) this.value).toClassConstructor());
				return;
			}
			return;
		case BYTECODE_END:
			this.mode = END;
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "bytecode.expression.close_brace");
			}
			return;
		case TYPE_ARGUMENTS_END:
		{
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "method.call.generic.close_bracket");
			}
			
			MethodCall mc = (MethodCall) this.value;
			GenericData genericData = mc.getGenericData();
			
			IToken next = token.next();
			int nextType = next.type();
			
			if (nextType == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.skip();
				IArguments arguments = this.parseArguments(pm, next.next());
				ApplyMethodCall amc = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), arguments);
				amc.setGenericData(genericData);
				
				this.value = amc;
				this.mode = PARAMETERS_END;
				return;
			}
			if (ParserUtil.isIdentifier(nextType))
			{
				pm.skip();
				this.value = mc.getReceiver();
				this.parseAccess(pm, token.next(), token.next().type(), token.next().nameValue(), null);
				
				if (this.value instanceof AbstractCall)
				{
					((AbstractCall) this.value).setGenericData(genericData);
				}
				if (this.value instanceof FieldAccess)
				{
					FieldAccess fieldAccess = (FieldAccess) this.value;
					mc.setName(fieldAccess.getName());
					this.value = mc;
				}
				return;
			}
			if (ParserUtil.isExpressionTerminator(nextType))
			{
				ApplyMethodCall amc = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), EmptyArguments.INSTANCE);
				amc.setGenericData(genericData);
				this.value = amc;
				this.mode = ACCESS;
				return;
			}
			
			SingleArgument argument = new SingleArgument();
			ApplyMethodCall amc = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), argument);
			amc.setGenericData(genericData);
			this.value = amc;
			
			this.parseApply(pm, next, argument, Operators.DEFAULT);
			this.mode = ACCESS;
			
			return;
		}
		case PARAMETERIZED_THIS_END:
			this.mode = ACCESS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "this.close_bracket");
			}
			return;
		case PARAMETERIZED_SUPER_END:
			this.mode = ACCESS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "super.close_bracket");
			}
			return;
		}
		
		if (ParserUtil.isCloseBracket(type) || type == BaseSymbols.COLON)
		{
			// Close bracket, end expression
			if (this.value != null)
			{
				this.valueConsumer.setValue(this.value);
			}
			pm.popParser(true);
			return;
		}
		
		if (this.mode == ACCESS)
		{
			if (type == BaseSymbols.DOT)
			{
				this.mode = DOT_ACCESS;
				this.explicitDot = true;
				return;
			}
			
			this.explicitDot = false;
			
			switch (type)
			{
			case DyvilKeywords.ELSE:
			case DyvilKeywords.CATCH:
			case DyvilKeywords.FINALLY:
				this.valueConsumer.setValue(this.value);
				pm.popParser(true);
				return;
			case BaseSymbols.EQUALS:
				this.parseAssignment(pm, token);
				return;
			case DyvilKeywords.AS:
				CastOperator co = new CastOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(co));
				this.value = co;
				return;
			case DyvilKeywords.IS:
				InstanceOfOperator io = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(io));
				this.value = io;
				return;
			case DyvilKeywords.MATCH:
				// Parse a match expression
				// e.g. int1 match { ... }, this match { ... }
				MatchExpr me = new MatchExpr(token.raw(), this.value);
				pm.pushParser(new MatchExpressionParser(me));
				this.value = me;
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				// Parse a subscript getter
				// e.g. this[1], array[0]
				SubscriptGetter getter = new SubscriptGetter(token, this.value);
				this.value = getter;
				this.mode = SUBSCRIPT_END;
				pm.pushParser(new ExpressionListParser((IValueList) getter.getArguments()));
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				// Parse an apply call
				// e.g. 1("a"), this("stuff"), "myString"(2)
				this.value = new ApplyMethodCall(this.value.getPosition(), this.value,
				                                 this.parseArguments(pm, token.next()));
				this.mode = PARAMETERS_END;
				return;
			}
			
			if (ParserUtil.isIdentifier(type))
			{
				this.parseIdentifierAccess(pm, token, type);
				return;
			}
			
			if (this.value != null)
			{
				if (this.operator != null)
				{
					this.valueConsumer.setValue(this.value);
					pm.popParser(true);
					return;
				}
				
				SingleArgument sa = new SingleArgument();
				ApplyMethodCall amc = new ApplyMethodCall(this.value.getPosition(), this.value, sa);
				this.parseApply(pm, token, sa, Operators.DEFAULT);
				pm.reparse();
				this.value = amc;
				return;
			}
		}
		if (this.mode == DOT_ACCESS)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.parseIdentifierAccess(pm, token, type);
				return;
			}
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				// IDENTIFIER . [
				MethodCall call = new MethodCall(token, this.value, null);
				pm.pushParser(new TypeListParser(call.getGenericData()));
				this.mode = TYPE_ARGUMENTS_END;
				this.value = call;
				return;
			}
			
			pm.report(MarkerMessages.createSyntaxError(token, "expression.dot.invalid", token.toString()));
			return;
		}
		
		pm.report(MarkerMessages.createSyntaxError(token, "expression.invalid", token.toString()));
		return;
	}
	
	/**
	 * Creates the body and initializes parsing for anonymous classes.
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param cc
	 * 		the anonymous class AST node.
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
	 * <ul> <li>{@link EmptyArguments} - For empty argument lists:<br> <code>
	 * this.call() </code> <li>{@link ArgumentList} - For simple indexed
	 * argument lists:<br> <code> this.call(1, "abc", null) </code> <li>{@link
	 * ArgumentMap} - For named argument lists / maps:<br> <code>
	 * this.call(index: 1, string: "abc") </code> </ul>
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param next
	 * 		the next token. The current token is assumed to be the opening
	 * 		parenthesis of the argument list.
	 *
	 * @return the appropriate AST representation for the type of argument list.
	 */
	public static IArguments parseArguments(IParserManager pm, IToken next)
	{
		int type = next.type();
		if (type == BaseSymbols.CLOSE_PARENTHESIS)
		{
			return EmptyArguments.VISIBLE;
		}
		if (ParserUtil.isIdentifier(type) && next.next().type() == BaseSymbols.COLON)
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
	 * 		the current parsing context manager.
	 * @param token
	 * 		the current token, has to be any {@code IDENTIFIER} token.
	 * @param type
	 * 		the {@code type} of the current {@code token}.
	 * @param name
	 * 		the {@code nameValue} of the {@code token}.
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
		
		// Name is not a compound operator (does not end with '=')
		if (Util.hasEq(name))
		{
			// e.g. this += that
			
			final Name newName = Util.removeEq(name);
			op = pm.getOperator(newName);
			if (op == null)
			{
				op = new Operator(newName, 1, Operator.INFIX_RIGHT);
			}
			else if (op.type == Operator.INFIX_LEFT)
			{
				// Compound Operators are always right-associative, so create a
				// copy
				op = new Operator(newName, op.precedence, Operator.INFIX_RIGHT);
			}
		}
		else
		{
			switch (nextType)
			{
			case BaseSymbols.OPEN_PARENTHESIS:
				MethodCall call = new MethodCall(token.raw(), this.value, name);
				call.setDotless(!this.explicitDot);
				this.value = call;
				this.mode = PARAMETERS_END;
				pm.skip();
				call.setArguments(this.parseArguments(pm, next.next()));
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				SubscriptGetter getter = new SubscriptGetter(token, new FieldAccess(token.raw(), this.value, name));
				this.value = getter;
				this.mode = SUBSCRIPT_END;
				pm.skip();
				pm.pushParser(new ExpressionListParser((IValueList) getter.getArguments()));
				return;
			case DyvilSymbols.ARROW_OPERATOR:
				MethodParameter parameter = new MethodParameter(token.raw(), token.nameValue(), Types.UNKNOWN,
				                                                EmptyModifiers.INSTANCE);
				LambdaExpr lambdaExpr = new LambdaExpr(next.raw(), parameter);
				this.mode = END;
				this.value = lambdaExpr;
				pm.pushParser(pm.newExpressionParser(lambdaExpr));
				pm.skip();
				return;
			}
			
			// ... EXPRESSION-TERMINATOR
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
				// ... IDENTIFIER NON-EXPRESSION-TERMINATOR
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
		
		SingleArgument sa = new SingleArgument();
		MethodCall call = new MethodCall(token, this.value, name, sa);
		call.setDotless(!this.explicitDot);
		
		this.value = call;
		this.mode = ACCESS;
		
		this.parseApply(pm, token.next(), sa, op == null ? Operators.DEFAULT : op);
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
				this.valueConsumer.setValue(this.value);
				pm.popParser(true);
				return;
			}
			if (p == operator.precedence)
			{
				// Handle associativity
				switch (operator.type)
				{
				case Operator.INFIX_LEFT:
					this.valueConsumer.setValue(this.value);
					pm.popParser(true);
					return;
				case Operator.INFIX_NONE:
					pm.report(MarkerMessages.createError(token, "expression.operator.invalid", name.toString()));
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
	 * <p>
	 * <pre>
	 * this 3
	 * print "abc"
	 * button { ... }
	 * </pre>
	 *
	 * @param pm
	 * 		the current parsing context manager
	 * @param token
	 * 		the first token of the expression that is a parameter to the APPLY
	 * 		method
	 * @param sa
	 * 		the argument container
	 * @param op
	 * 		the operator that precedes this call. Can be null.
	 */
	private void parseApply(IParserManager pm, IToken token, SingleArgument sa, Operator op)
	{
		if (token.type() == BaseSymbols.OPEN_CURLY_BRACKET)
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
	 * 		the current parsing context manager
	 * @param token
	 * 		the current token, i.e. the '=' sign
	 */
	private void parseAssignment(IParserManager pm, IToken token)
	{
		if (this.value != null)
		{
			ICodePosition position = this.value.getPosition();
			int valueType = this.value.valueTag();
			switch (valueType)
			{
			case IValue.FIELD_ACCESS:
			{
				FieldAccess fa = (FieldAccess) this.value;
				FieldAssignment assign = new FieldAssignment(position, fa.getInstance(), fa.getName());
				this.value = assign;
				pm.pushParser(pm.newExpressionParser(assign));
				return;
			}
			case IValue.APPLY_CALL:
			{
				ApplyMethodCall call = (ApplyMethodCall) this.value;
				UpdateMethodCall updateCall = new UpdateMethodCall(position, call.getReceiver(), call.getArguments());
				this.value = updateCall;
				pm.pushParser(pm.newExpressionParser(updateCall));
				return;
			}
			case IValue.METHOD_CALL:
			{
				MethodCall call = (MethodCall) this.value;
				FieldAccess fa = new FieldAccess(position, call.getReceiver(), call.getName());
				UpdateMethodCall updateCall = new UpdateMethodCall(position, fa, call.getArguments());
				this.value = updateCall;
				pm.pushParser(pm.newExpressionParser(updateCall));
				return;
			}
			case IValue.SUBSCRIPT_GET:
			{
				SubscriptGetter getter = (SubscriptGetter) this.value;
				SubscriptSetter setter = new SubscriptSetter(position, getter.getReceiver(), getter.getArguments());
				this.value = setter;
				pm.pushParser(pm.newExpressionParser(setter));
				return;
			}
			}
		}

		pm.report(MarkerMessages.createSyntaxError(token, "assignment.invalid", token));
		this.mode = VALUE;
		this.value = null;
		return;
	}
	
	private boolean parseKeyword(IParserManager pm, IToken token, int type)
	{
		switch (type)
		{
		case DyvilKeywords.NULL:
			this.value = new NullValue(token.raw());
			this.mode = ACCESS;
			return true;
		case DyvilKeywords.NIL:
			this.value = new NilExpr(token.raw());
			this.mode = ACCESS;
			return true;
		case DyvilKeywords.TRUE:
			this.value = new BooleanValue(token.raw(), true);
			this.mode = ACCESS;
			return true;
		case DyvilKeywords.FALSE:
			this.value = new BooleanValue(token.raw(), false);
			this.mode = ACCESS;
			return true;
		case DyvilKeywords.THIS:
		{
			IToken next = token.next();
			switch (next.type())
			{
			// this[type]
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				ThisExpr tv = new ThisExpr(token.raw());
				this.mode = PARAMETERIZED_THIS_END;
				this.value = tv;
				pm.skip();
				pm.pushParser(new TypeParser(tv));
				return true;
			case BaseSymbols.DOT:
				// this.new
				IToken next2 = next.next();
				if (next2.type() == DyvilKeywords.NEW)
				{
					this.value = new InitializerCall(next2.raw(), false);
					pm.skip(2);
					this.mode = CONSTRUCTOR_PARAMETERS;
					return true;
				}
			}
			this.value = new ThisExpr(token.raw());
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.SUPER:
		{
			IToken next = token.next();
			switch (next.type())
			{
			// super[type]
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				SuperExpr sv = new SuperExpr(token.raw());
				this.mode = PARAMETERIZED_SUPER_END;
				this.value = sv;
				pm.skip();
				pm.pushParser(new TypeParser(sv));
				return true;
			case BaseSymbols.DOT:
				// super.new
				IToken next2 = next.next();
				if (next2.type() == DyvilKeywords.NEW)
				{
					this.value = new InitializerCall(next2.raw(), true);
					pm.skip(2);
					this.mode = CONSTRUCTOR_PARAMETERS;
					return true;
				}
			}
			this.value = new SuperExpr(token.raw());
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.CLASS:
		{
			ClassOperator co = new ClassOperator(token);
			this.value = co;
			pm.pushParser(pm.newTypeParser(co));
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.TYPE:
		{
			TypeOperator to = new TypeOperator(token);
			this.value = to;
			pm.pushParser(pm.newTypeParser(to));
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.NEW:
		{
			ConstructorCall call = new ConstructorCall(token);
			this.mode = CONSTRUCTOR;
			this.value = call;
			pm.pushParser(pm.newTypeParser(this));
			return true;
		}
		case DyvilKeywords.RETURN:
		{
			ReturnStatement rs = new ReturnStatement(token.raw());
			this.value = rs;
			pm.pushParser(pm.newExpressionParser(rs));
			return true;
		}
		case DyvilKeywords.IF:
		{
			IfStatement is = new IfStatement(token.raw());
			this.value = is;
			pm.pushParser(new IfStatementParser(is));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.ELSE:
		{
			if (!(this.parent instanceof IfStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.else");
				return true;
			}
			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case DyvilKeywords.WHILE:
		{
			WhileStatement statement = new WhileStatement(token);
			this.value = statement;
			pm.pushParser(new WhileStatementParser(statement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.DO:
		{
			DoStatement statement = new DoStatement(token);
			this.value = statement;
			pm.pushParser(new DoStatementParser(statement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.FOR:
		{
			pm.pushParser(new ForStatementParser(this.valueConsumer, token.raw()));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.BREAK:
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
		case DyvilKeywords.CONTINUE:
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
		case DyvilKeywords.GOTO:
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
		case DyvilKeywords.TRY:
		{
			TryStatement statement = new TryStatement(token.raw());
			pm.pushParser(new TryStatementParser(statement));
			this.mode = END;
			this.value = statement;
			return true;
		}
		case DyvilKeywords.CATCH:
		{
			if (!(this.parent instanceof TryStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.catch");
				return true;
			}
			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case DyvilKeywords.FINALLY:
		{
			if (!(this.parent instanceof TryStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.finally");
				return true;
			}
			this.valueConsumer.setValue(this.value);
			pm.popParser(true);
			return true;
		}
		case DyvilKeywords.THROW:
		{
			ThrowStatement statement = new ThrowStatement(token.raw());
			pm.pushParser(pm.newExpressionParser(statement));
			this.mode = END;
			this.value = statement;
			return true;
		}
		case DyvilKeywords.SYNCHRONIZED:
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
			this.value.setType(type);
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
}
