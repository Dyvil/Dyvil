package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.parameter.*;
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
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public final class ExpressionParser extends Parser implements IValueConsumer
{
	protected static final int VALUE      = 0x1;
	protected static final int ACCESS     = 0x2;
	protected static final int DOT_ACCESS = 0x4;

	protected static final int PARAMETERS                 = 0x8;
	protected static final int PARAMETERS_END             = 0x10;
	protected static final int CONSTRUCTOR_PARAMETERS     = 0x20;
	protected static final int CONSTRUCTOR_PARAMETERS_END = 0x40;
	protected static final int ANONYMOUS_CLASS_END        = 0x80;
	protected static final int SUBSCRIPT_END              = 0x100;
	protected static final int TYPE_ARGUMENTS_END         = 0x200;
	
	protected static final int PARAMETRIC_THIS_END  = 0x400;
	protected static final int PARAMETRIC_SUPER_END = 0x800;
	
	protected IValueConsumer valueConsumer;
	
	private IValue value;
	
	private boolean  explicitDot;
	private Operator operator;
	
	public ExpressionParser(IValueConsumer valueConsumer)
	{
		this.mode = VALUE;
		this.valueConsumer = valueConsumer;
	}
	
	public ExpressionParser withOperator(Operator operator)
	{
		this.operator = operator;
		return this;
	}

	private void end(IParserManager pm)
	{
		if (this.value != null)
		{
			this.valueConsumer.setValue(this.value);
		}
		pm.popParser(true);
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case Tokens.EOF:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.COMMA:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
			this.end(pm);
			return;
		}
		
		switch (this.mode)
		{
		case END:
			this.end(pm);
			return;
		case VALUE:
			switch (type)
			{
			case Tokens.STRING:
				this.value = new StringValue(token.raw(), token.stringValue());
				this.mode = ACCESS;
				return;
			case Tokens.STRING_START:
			{
				final StringInterpolationExpr stringInterpolation = new StringInterpolationExpr(token);
				this.value = stringInterpolation;
				this.mode = ACCESS;
				pm.pushParser(new StingInterpolationParser(stringInterpolation), true);
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
				// _ ...
				this.value = new WildcardValue(token.raw());
				this.mode = ACCESS;
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				// ( ...
				final IToken next = token.next();

				if (next.type() != BaseSymbols.CLOSE_PARENTHESIS)
				{
					// ( ...
					pm.pushParser(new LambdaOrTupleParser(this), true);
					this.mode = ACCESS;
					return;
				}

				final IToken next2 = next.next();
				if (next2.type() == DyvilSymbols.ARROW_OPERATOR)
				{
					// () => ...
					final LambdaExpr lambda = new LambdaExpr(next2.raw());
					this.value = lambda;
					pm.skip(2);
					pm.pushParser(pm.newExpressionParser(lambda));
					this.mode = ACCESS;
					return;
				}

				// ()
				this.value = new VoidValue(token.to(token.next()));
				pm.skip();
				this.mode = ACCESS;
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				// [ ...
				this.mode = ACCESS;
				pm.pushParser(new ArrayLiteralParser(this), true);
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				// { ...
				this.mode = ACCESS;
				pm.pushParser(new StatementListParser(this), true);
				return;
			case DyvilSymbols.AT:
				// @ ...
				Annotation a = new Annotation();
				pm.pushParser(new AnnotationParser(a));
				this.value = new AnnotationValue(a);
				this.mode = END;
				return;
			case DyvilSymbols.ARROW_OPERATOR:
			{
				// => ...
				LambdaExpr lambda = new LambdaExpr(token.raw());
				this.value = lambda;
				this.mode = ACCESS;
				pm.pushParser(pm.newExpressionParser(lambda));
				return;
			}
			}

			if ((type & Tokens.IDENTIFIER) != 0)
			{
				// IDENTIFIER ...
				this.parseIdentifierAccess(pm, token);
				return;
			}
			if (this.parseKeyword(pm, token, type))
			{
				// keyword ...
				return;
			}
			
			this.mode = ACCESS;
			// Leave the big switch and jump right over to the ACCESS
			// section
			break;
		case ANONYMOUS_CLASS_END:
			// new ... { ... } ...
			//               ^
			this.value.expandPosition(token);
			this.mode = ACCESS;
			
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				pm.report(token, "class.anonymous.body.end");
			}
			
			return;
		case PARAMETERS_END:
			// ... ( ... )
			//           ^
			this.mode = ACCESS;
			this.value.expandPosition(token);
			
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "method.call.close_paren");
			}
			
			return;
		case SUBSCRIPT_END:
			// ... [ ... ]
			//           ^
			this.mode = ACCESS;
			this.value.expandPosition(token);
			
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "method.subscript.close_bracket");
			}
			
			return;
		case CONSTRUCTOR_PARAMETERS:
		{
			// new ...
			//         ^

			final ConstructorCall constructorCall = (ConstructorCall) this.value;
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				// new ... {
				this.parseBody(pm, constructorCall.toClassConstructor());
				return;
			}
		}
		case PARAMETERS:
		{
			final ICall call = (ICall) this.value;

			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				// new ... (
				final IArguments arguments = parseArguments(pm, token.next());
				call.setArguments(arguments);
				this.mode = CONSTRUCTOR_PARAMETERS_END;
				return;
			}

			// new ... ;
			if (ParserUtil.isExpressionTerminator(type))
			{
				this.mode = ACCESS;
				pm.reparse();
				return;
			}
			
			final SingleArgument argument = new SingleArgument();
			call.setArguments(argument);

			pm.pushParser(pm.newExpressionParser(argument).withOperator(Operators.DEFAULT), true);
			this.mode = END;
			return;
		}
		case CONSTRUCTOR_PARAMETERS_END:
			// new ... ( ... )
			//               ^
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "constructor.call.close_paren");
			}
			this.value.expandPosition(token);
			this.mode = ACCESS;

			if (token.next().type() == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				// new ... ( ... ) { ...

				pm.skip();
				this.parseBody(pm, ((ConstructorCall) this.value).toClassConstructor());
				return;
			}
			return;
		case TYPE_ARGUMENTS_END:
		{
			// ... .[ ... ]
			//            ^

			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "method.call.generic.close_bracket");
			}
			
			final MethodCall mc = (MethodCall) this.value;
			final GenericData genericData = mc.getGenericData();
			
			final IToken next = token.next();
			final int nextType = next.type();
			
			if (nextType == BaseSymbols.OPEN_PARENTHESIS)
			{
				// ... .[ ... ] ( ...

				pm.skip();
				IArguments arguments = parseArguments(pm, next.next());
				ApplyMethodCall amc = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), arguments);
				amc.setGenericData(genericData);
				
				this.value = amc;
				this.mode = PARAMETERS_END;
				return;
			}
			if (ParserUtil.isIdentifier(nextType))
			{
				// ... .[ ... ] IDENTIFIER ...
				pm.skip();
				this.value = mc.getReceiver();
				this.parseIdentifierAccess(pm, token.next(), token.next().nameValue(), null);
				
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
				// ... .[ ... ] ;

				ApplyMethodCall amc = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), EmptyArguments.INSTANCE);
				amc.setGenericData(genericData);
				this.value = amc;
				this.mode = ACCESS;
				return;
			}

			// ... .[ ... ] ...
			final SingleArgument argument = new SingleArgument();

			final ApplyMethodCall applyCall = new ApplyMethodCall(mc.getPosition(), mc.getReceiver(), argument);
			applyCall.setGenericData(genericData);
			this.value = applyCall;
			
			this.parseApply(pm, next, argument, Operators.DEFAULT);
			this.mode = ACCESS;
			
			return;
		}
		case PARAMETRIC_THIS_END:
			// this[ ... ]
			//           ^

			this.mode = ACCESS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "this.close_bracket");
			}
			return;
		case PARAMETRIC_SUPER_END:
			// super[ ... ]
			//            ^

			this.mode = ACCESS;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "super.close_bracket");
			}
			return;
		}
		
		if (ParserUtil.isCloseBracket(type) || type == BaseSymbols.COLON)
		{
			// ... ] OR ... :

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
				// ... .

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
				this.end(pm);
				return;
			case BaseSymbols.EQUALS:
				// EXPRESSION =

				this.parseAssignment(pm, token);
				return;
			case DyvilKeywords.AS:
			{
				// EXPRESSION as

				final CastOperator castOperator = new CastOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(castOperator));
				this.value = castOperator;
				return;
			}
			case DyvilKeywords.IS:
			{
				// EXPRESSION is

				final InstanceOfOperator instanceOfOperator = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(pm.newTypeParser(instanceOfOperator));
				this.value = instanceOfOperator;
				return;
			}
			case DyvilKeywords.MATCH:
				// EXPRESSION match

				// Parse a match expression
				// e.g. int1 match { ... }, this match { ... }
				MatchExpr me = new MatchExpr(token.raw(), this.value);
				pm.pushParser(new MatchExpressionParser(me));
				this.value = me;
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				// EXPRESSION [

				// Parse a subscript getter
				// e.g. this[1], array[0]
				SubscriptAccess getter = new SubscriptAccess(token, this.value);
				this.value = getter;
				this.mode = SUBSCRIPT_END;
				pm.pushParser(new ExpressionListParser((IValueList) getter.getArguments()));
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				// EXPRESSION (

				// Parse an apply call
				// e.g. 1("a"), this("stuff"), "myString"(2)
				this.value = new ApplyMethodCall(this.value.getPosition(), this.value,
				                                 parseArguments(pm, token.next()));
				this.mode = PARAMETERS_END;
				return;
			}
			
			if (ParserUtil.isIdentifier(type))
			{
				// EXPRESSION IDENTIFIER
				this.parseAccess(pm, token);
				return;
			}
			
			if (this.value != null)
			{
				// EXPRESSION EXPRESSION -> ... ( EXPRESSION )

				if (this.operator != null)
				{
					this.end(pm);
					return;
				}
				
				final SingleArgument argument = new SingleArgument();
				final ApplyMethodCall applyCall = new ApplyMethodCall(this.value.getPosition(), this.value, argument);

				this.parseApply(pm, token, argument, Operators.DEFAULT);
				pm.reparse();
				this.value = applyCall;
				return;
			}
		}
		if (this.mode == DOT_ACCESS)
		{
			// ... .

			if (ParserUtil.isIdentifier(type))
			{
				// EXPRESSION . IDENTIFIER

				this.parseAccess(pm, token);
				return;
			}
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				// EXPRESSION . [
				MethodCall call = new MethodCall(token, this.value, null);
				pm.pushParser(new TypeListParser(call.getGenericData()));
				this.mode = TYPE_ARGUMENTS_END;
				this.value = call;
				return;
			}

			pm.report(Markers.syntaxError(token, "expression.dot.invalid", token.toString()));
			return;
		}
		
		pm.report(Markers.syntaxError(token, "expression.invalid", token.toString()));
		return;
	}
	
	/**
	 * Creates the body and initializes parsing for anonymous classes.
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param classConstructor
	 * 		the anonymous class AST node.
	 */
	private void parseBody(IParserManager pm, ClassConstructor classConstructor)
	{
		final IClass nestedClass = classConstructor.getNestedClass();
		final IClassBody body = nestedClass.getBody();

		pm.pushParser(new ClassBodyParser(nestedClass, body));
		this.mode = ANONYMOUS_CLASS_END;
		this.value = classConstructor;
		return;
	}
	
	/**
	 * Parses an argument list and creates the appropriate AST representation. The following instances can be created by
	 * this method:
	 * <p>
	 * <ul> <li>{@link EmptyArguments} - For empty argument lists:<br> <code> this.call() </code> <li>{@link
	 * ArgumentList} - For simple indexed argument lists:<br> <code> this.call(1, "abc", null) </code> <li>{@link
	 * ArgumentMap} - For named argument lists / maps:<br> <code> this.call(index: 1, string: "abc") </code> </ul>
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param next
	 * 		the next token. The current token is assumed to be the opening parenthesis of the argument list.
	 *
	 * @return the appropriate AST representation for the type of argument list.
	 */
	public static IArguments parseArguments(IParserManager pm, IToken next)
	{
		final int type = next.type();

		if (type == BaseSymbols.CLOSE_PARENTHESIS)
		{
			return EmptyArguments.VISIBLE;
		}
		if (ParserUtil.isIdentifier(type) && next.next().type() == BaseSymbols.COLON)
		{
			final ArgumentMap map = new ArgumentMap();
			pm.pushParser(new ExpressionMapParser(map));
			return map;
		}
		
		final ArgumentList list = new ArgumentList();
		pm.pushParser(new ExpressionListParser(list));
		return list;
	}

	private void parseAccess(IParserManager pm, IToken token)
	{
		final Name name = token.nameValue();
		this.parseAccess(pm, token, name, pm.getOperator(name));
	}
	
	/**
	 * Checks for operator precedence and associativity, and POPs if necessary.
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param token
	 * 		the current token, has to be any {@code IDENTIFIER} token.
	 * @param name
	 * 		the {@code nameValue} of the {@code token}.
	 * @param operator
	 * 		the operator corresponding to the {@code name}, or {@code null}
	 */
	private void parseAccess(IParserManager pm, IToken token, Name name, Operator operator)
	{
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
				case Operator.INFIX_NONE:
					pm.report(Markers.syntaxError(token, "expression.operator.invalid", name.toString()));
					return;
				case Operator.INFIX_LEFT:
					if (this.operator.type != Operator.INFIX_RIGHT)
					{
						this.valueConsumer.setValue(this.value);
						pm.popParser(true);
						return;
					}
					break;
				case Operator.INFIX_RIGHT:
					break;
				}
			}
		}

		this.parseIdentifierAccess(pm, token, name, operator);
		return;
	}

	private void parseIdentifierAccess(IParserManager pm, IToken token)
	{
		final Name name = token.nameValue();
		this.parseIdentifierAccess(pm, token, name, pm.getOperator(name));
	}

	/**
	 * Parses an ACCESS sequence.
	 *
	 * @param pm
	 * 		the current parsing context manager.
	 * @param token
	 * 		the current token, has to be any {@code IDENTIFIER} token.
	 * @param name
	 * 		the {@code nameValue} of the {@code token}.
	 * @param operator
	 * 		the operator corresponding to the {@code name}, or {@code null}
	 */
	private void parseIdentifierAccess(IParserManager pm, IToken token, Name name, Operator operator)
	{
		final IToken next = token.next();
		final int nextType = next.type();

		if (operator != null && !this.explicitDot)
		{
			if (this.value == null)
			{
				// OPERATOR EXPRESSION
				// token    next

				final SingleArgument argument = new SingleArgument();
				final MethodCall call = new MethodCall(token, null, name, argument);

				call.setDotless(!this.explicitDot);
				this.value = call;
				this.mode = ACCESS;

				this.parseApply(pm, token.next(), argument, operator);
				return;
			}

			// EXPRESSION OPERATOR EXPRESSION
			//            token    next

			final MethodCall call = new MethodCall(token, this.value, name);
			call.setDotless(!this.explicitDot);
			this.value = call;
			this.mode = ACCESS;

			if (operator.type != Operator.POSTFIX && !ParserUtil.isExpressionTerminator(nextType))
			{
				SingleArgument sa = new SingleArgument();
				call.setArguments(sa);

				this.parseApply(pm, token, sa, operator);
			}
			return;
		}

		if (Util.hasEq(name))
		{
			// Identifier is a compound operator (ends with '=' or '$eq')
			// e.g. this += that

			final Name newName = Util.removeEq(name);
			operator = pm.getOperator(newName);
			if (operator == null)
			{
				operator = new Operator(newName, 1, Operator.INFIX_RIGHT);
			}
			else if (operator.type == Operator.INFIX_LEFT)
			{
				// Compound Operators are always right-associative, so create a
				// copy
				operator = new Operator(newName, operator.precedence, Operator.INFIX_RIGHT);
			}
		}
		else
		{
			// Identifier is neither a defined nor a compound operator

			switch (nextType)
			{
			case BaseSymbols.OPEN_PARENTHESIS:
			{
				// IDENTIFIER (
				final MethodCall call = new MethodCall(token.raw(), this.value, name);
				call.setArguments(parseArguments(pm, next.next()));
				call.setDotless(!this.explicitDot);
				this.value = call;

				this.mode = PARAMETERS_END;
				pm.skip();
				return;
			}
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			{
				// IDENTIFIER [

				final FieldAccess fieldAccess = new FieldAccess(token.raw(), this.value, name);
				final SubscriptAccess subscriptAccess = new SubscriptAccess(token, fieldAccess);

				this.value = subscriptAccess;
				this.mode = SUBSCRIPT_END;
				pm.skip();
				pm.pushParser(new ExpressionListParser((IValueList) subscriptAccess.getArguments()));
				return;
			}
			case DyvilSymbols.ARROW_OPERATOR:
				// IDENTIFIER => ...
				// Lambda Expression with one untyped parameter

				final MethodParameter parameter = new MethodParameter(token.raw(), token.nameValue(), Types.UNKNOWN,
				                                                      EmptyModifiers.INSTANCE);
				final LambdaExpr lambdaExpr = new LambdaExpr(next.raw(), parameter);

				this.mode = END;
				this.value = lambdaExpr;
				pm.pushParser(pm.newExpressionParser(lambdaExpr));
				pm.skip();
				return;
			}

			if (ParserUtil.isExpressionTerminator(nextType))
			{
				// ... IDENTIFIER EXPRESSION-TERMINATOR
				// e.g. this.someField ;
				final FieldAccess access = new FieldAccess(token, this.value, name);
				access.setDotless(!this.explicitDot);
				this.value = access;
				this.mode = ACCESS;
				return;
			}

			// ... IDENTIFIER IDENTIFIER ...
			// e.g. this call ...
			if (ParserUtil.isIdentifier(nextType))
			{
				// ... OPERATOR ...
				// e.g. this + that
				// ... IDENTIFIER NON-EXPRESSION-TERMINATOR
				// e.g. this.plus that
				if (ParserUtil.isOperator(pm, next, nextType) || !ParserUtil.isExpressionTerminator(next.next().type()))
				{
					final FieldAccess access = new FieldAccess(token, this.value, name);
					access.setDotless(!this.explicitDot);
					this.value = access;
					this.mode = ACCESS;
					return;
				}
			}

			// Else -> Fallback to single-argument call
			// e.g. this.call 10;
		}

		final SingleArgument argument = new SingleArgument();
		final MethodCall call = new MethodCall(token, this.value, name, argument);
		call.setDotless(!this.explicitDot);

		this.value = call;
		this.mode = ACCESS;

		this.parseApply(pm, token.next(), argument, operator == null ? Operators.DEFAULT : operator);
		return;
	}

	/**
	 * Parses an APPLY call, without parenthesis. It might be possible that {@code pm.reparse()} has to be called after
	 * this method, depending on the token that is passed. E.g.:
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
	 * 		the first token of the expression that is a parameter to the APPLY method
	 * @param argument
	 * 		the argument container
	 * @param operator
	 * 		the operator that precedes this call. Can be null.
	 */
	private void parseApply(IParserManager pm, IToken token, SingleArgument argument, Operator operator)
	{
		if (token.type() == BaseSymbols.OPEN_CURLY_BRACKET)
		{
			final StatementListParser statementListParser = new StatementListParser(argument);
			statementListParser.setApplied(true);
			pm.pushParser(statementListParser);
			return;
		}
		
		pm.pushParser(pm.newExpressionParser(argument).withOperator(operator));
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
			final ICodePosition position = this.value.getPosition();
			final int valueType = this.value.valueTag();

			switch (valueType)
			{
			case IValue.FIELD_ACCESS:
			{
				// ... IDENTIFIER =

				final FieldAccess access = (FieldAccess) this.value;
				final FieldAssignment assignment = new FieldAssignment(position, access.getInstance(),
				                                                       access.getName());
				this.value = assignment;
				pm.pushParser(pm.newExpressionParser(assignment));
				return;
			}
			case IValue.APPLY_CALL:
			{
				// ... ( ... ) =

				final ApplyMethodCall applyCall = (ApplyMethodCall) this.value;
				final UpdateMethodCall updateCall = new UpdateMethodCall(position, applyCall.getReceiver(),
				                                                         applyCall.getArguments());

				this.value = updateCall;
				pm.pushParser(pm.newExpressionParser(updateCall));
				return;
			}
			case IValue.METHOD_CALL:
			{
				// ... IDENTIFIER ( ... ) =

				final MethodCall call = (MethodCall) this.value;
				final FieldAccess access = new FieldAccess(position, call.getReceiver(), call.getName());
				final UpdateMethodCall updateCall = new UpdateMethodCall(position, access, call.getArguments());

				this.value = updateCall;
				pm.pushParser(pm.newExpressionParser(updateCall));
				return;
			}
			case IValue.SUBSCRIPT_GET:
			{
				// ... [ ... ] =

				final SubscriptAccess subscriptAccess = (SubscriptAccess) this.value;
				final SubscriptAssignment subscriptAssignment = new SubscriptAssignment(position,
				                                                                        subscriptAccess.getReceiver(),
				                                                                        subscriptAccess.getArguments());

				this.value = subscriptAssignment;
				pm.pushParser(pm.newExpressionParser(subscriptAssignment));
				return;
			}
			}
		}

		pm.report(Markers.syntaxError(token, "assignment.invalid", token));
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
			// this ...

			final IToken next = token.next();
			switch (next.type())
			{
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			{
				// this [ ... ]
				final ThisExpr thisExpr = new ThisExpr(token.raw());

				this.mode = PARAMETRIC_THIS_END;
				this.value = thisExpr;
				pm.skip();
				pm.pushParser(new TypeParser(thisExpr));
				return true;
			}
			case BaseSymbols.DOT:
				// this.init OR this.new
				if (this.parseInitializer(pm, next, false))
				{
					return true;
				}
			}
			this.value = new ThisExpr(token.raw());
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.SUPER:
		{
			final IToken next = token.next();
			switch (next.type())
			{
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			{
				// super [ ... ]
				final SuperExpr superExpr = new SuperExpr(token.raw());

				this.mode = PARAMETRIC_SUPER_END;
				this.value = superExpr;
				pm.skip();
				pm.pushParser(new TypeParser(superExpr));
				return true;
			}
			case BaseSymbols.DOT:
				// super.init OR super.new
				if (this.parseInitializer(pm, next, true))
				{
					return true;
				}
			}
			this.value = new SuperExpr(token.raw());
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.CLASS:
		{
			// class ...

			final ClassOperator classOperator = new ClassOperator(token);
			this.value = classOperator;

			pm.pushParser(pm.newTypeParser(classOperator));
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.TYPE:
		{
			// type ...

			TypeOperator typeOperator = new TypeOperator(token);
			this.value = typeOperator;

			pm.pushParser(pm.newTypeParser(typeOperator));
			this.mode = ACCESS;
			return true;
		}
		case DyvilKeywords.NEW:
		{
			// new ...

			final ConstructorCall call = new ConstructorCall(token);
			this.value = call;

			this.mode = CONSTRUCTOR_PARAMETERS;
			pm.pushParser(pm.newTypeParser(call));
			return true;
		}
		case DyvilKeywords.RETURN:
		{
			// return ...

			ReturnStatement returnStatement = new ReturnStatement(token.raw());
			this.value = returnStatement;

			pm.pushParser(pm.newExpressionParser(returnStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.IF:
		{
			// if ...

			final IfStatement ifStatement = new IfStatement(token.raw());
			this.value = ifStatement;

			pm.pushParser(new IfStatementParser(ifStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.ELSE:
		{
			// ... else

			if (!(this.parent instanceof IfStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.else");
				return true;
			}

			this.end(pm);
			return true;
		}
		case DyvilKeywords.WHILE:
		{
			// while ...

			final WhileStatement whileStatement = new WhileStatement(token);
			this.value = whileStatement;

			pm.pushParser(new WhileStatementParser(whileStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.DO:
		{
			// do ...

			final DoStatement doStatement = new DoStatement(token);
			this.value = doStatement;

			pm.pushParser(new DoStatementParser(doStatement));
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
			final BreakStatement breakStatement = new BreakStatement(token);
			this.value = breakStatement;

			final IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				breakStatement.setName(next.nameValue());
				pm.skip();
			}

			this.mode = END;
			return true;
		}
		case DyvilKeywords.CONTINUE:
		{
			final ContinueStatement continueStatement = new ContinueStatement(token);
			this.value = continueStatement;

			final IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				continueStatement.setName(next.nameValue());
				pm.skip();
			}

			this.mode = END;
			return true;
		}
		case DyvilKeywords.GOTO:
		{
			GoToStatement statement = new GoToStatement(token);
			this.value = statement;

			final IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				statement.setName(next.nameValue());
				pm.skip();
			}

			this.mode = END;
			return true;
		}
		case DyvilKeywords.TRY:
		{
			// try ...

			final TryStatement tryStatement = new TryStatement(token.raw());
			this.value = tryStatement;

			pm.pushParser(new TryStatementParser(tryStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.CATCH:
		{
			// ... catch ...

			if (!(this.parent instanceof TryStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.catch");
				return true;
			}

			this.end(pm);
			return true;
		}
		case DyvilKeywords.FINALLY:
		{
			// ... finally ...

			if (!(this.parent instanceof TryStatementParser) && !(this.parent instanceof ExpressionParser))
			{
				pm.report(token, "expression.finally");
				return true;
			}

			this.end(pm);
			return true;
		}
		case DyvilKeywords.THROW:
		{
			final ThrowStatement throwStatement = new ThrowStatement(token.raw());
			this.value = throwStatement;

			pm.pushParser(pm.newExpressionParser(throwStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.SYNCHRONIZED:
		{
			final SyncStatement syncStatement = new SyncStatement(token.raw());
			this.value = syncStatement;

			pm.pushParser(new SyncStatementParser(syncStatement));
			this.mode = END;
			return true;
		}
		}
		return false;
	}

	/**
	 * Parses a {@code this} or {@code super} initializer.
	 *
	 * @param pm
	 * 		the current parsing context manager
	 * @param next
	 * 		the token after the {@code this} or {@code super} token. Usually a {@code DOT .}.
	 * @param isSuper
	 * 		{@code true} if the previous token was {@code super}, {@code false} for {@code this}.
	 *
	 * @return {@code true}, iff an initializer could be parsed successfully
	 */
	private boolean parseInitializer(IParserManager pm, IToken next, boolean isSuper)
	{
		final IToken next2 = next.next();

		switch (next2.type())
		{
		case DyvilKeywords.NEW:
			pm.report(Markers.syntaxWarning(next2, "constructor.initializer.new"));
			// Fallthrough
		case DyvilKeywords.INIT:
			this.value = new InitializerCall(next2.raw(), isSuper);
			pm.skip(2);
			this.mode = PARAMETERS;
			return true;
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
}
