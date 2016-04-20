package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.operator.OperatorChain;
import dyvil.tools.compiler.ast.operator.PostfixCall;
import dyvil.tools.compiler.ast.operator.PrefixCall;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.statement.ReturnStatement;
import dyvil.tools.compiler.ast.statement.SyncStatement;
import dyvil.tools.compiler.ast.statement.control.BreakStatement;
import dyvil.tools.compiler.ast.statement.control.ContinueStatement;
import dyvil.tools.compiler.ast.statement.control.GoToStatement;
import dyvil.tools.compiler.ast.statement.exception.ThrowStatement;
import dyvil.tools.compiler.ast.statement.exception.TryStatement;
import dyvil.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvil.tools.compiler.ast.statement.loop.WhileStatement;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.statement.*;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.ParserUtil.*;

public final class ExpressionParser extends Parser implements IValueConsumer
{
	// Modes

	protected static final int VALUE              = 0;
	protected static final int ACCESS             = 1;
	protected static final int DOT_ACCESS         = 1 << 1;
	protected static final int PARAMETERS_END     = 1 << 2;
	protected static final int SUBSCRIPT_END      = 1 << 3;
	protected static final int TYPE_ARGUMENTS_END = 1 << 4;

	// Flags

	private static final int IGNORE_APPLY    = 0b00001;
	private static final int IGNORE_OPERATOR = 0b00010;
	public static final  int IGNORE_COLON    = 0b00100;
	public static final  int IGNORE_LAMBDA   = 0b01000;
	public static final  int IGNORE_CLOSURE  = 0b10000;

	// ----------

	protected IValueConsumer valueConsumer;

	private IValue value;

	private int flags;

	public ExpressionParser(IValueConsumer valueConsumer)
	{
		this.valueConsumer = valueConsumer;
		// this.mode = VALUE;
	}

	public boolean hasFlag(int flag)
	{
		return (this.flags & flag) != 0;
	}

	public void addFlag(int flag)
	{
		this.flags |= flag;
	}

	public ExpressionParser withFlag(int flag)
	{
		this.flags |= flag;
		return this;
	}

	public void removeFlag(int flag)
	{
		this.flags &= ~flag;
	}

	private void end(IParserManager pm, boolean reparse)
	{
		if (this.value != null)
		{
			this.valueConsumer.setValue(this.value);
		}
		pm.popParser(reparse);
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
			this.end(pm, true);
			return;
		}

		switch (this.mode)
		{
		case END:
			this.end(pm, true);
			return;
		case VALUE:
			if ((type & Tokens.IDENTIFIER) != 0)
			{
				// IDENTIFIER ...
				this.parseInfixAccess(pm, token);
				return;
			}
			if (this.parseValue(pm, token, type))
			{
				// keyword ...
				return;
			}

			this.mode = ACCESS;
			// Leave the big switch and jump right over to the ACCESS
			// section
			break;
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
		case TYPE_ARGUMENTS_END:
			if (!TypeParser.isGenericEnd(token, type))
			{
				pm.reparse();
				pm.report(token, "method.call.generic.close_angle");
			}

			final IToken next = token.next();
			if (next.type() == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.skip();
				ArgumentListParser.parseArguments(pm, next.next(), (ICall) this.value);
				this.mode = PARAMETERS_END;
				return;
			}

			this.mode = ACCESS;
			return;
		}

		if (ParserUtil.isCloseBracket(type) || type == BaseSymbols.COLON && this.hasFlag(IGNORE_COLON))
		{
			// ... ]

			// Close bracket, end expression
			this.end(pm, true);
			return;
		}

		if (this.mode == ACCESS)
		{
			if (type == BaseSymbols.DOT)
			{
				// ... .

				this.mode = DOT_ACCESS;
				return;
			}

			switch (type)
			{
			case DyvilSymbols.ARROW_RIGHT:
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
				if (!this.hasFlag(IGNORE_LAMBDA))
				{
					break;
				}
				// Fallthrough
			case DyvilKeywords.ELSE:
			case DyvilKeywords.CATCH:
			case DyvilKeywords.FINALLY:
			case DyvilKeywords.WHILE:
				this.end(pm, true);
				return;
			case DyvilKeywords.AS:
			{
				// EXPRESSION as

				final CastOperator castOperator = new CastOperator(token.raw(), this.value);
				pm.pushParser(new TypeParser(castOperator));
				this.value = castOperator;
				return;
			}
			case DyvilKeywords.IS:
			{
				// EXPRESSION is

				final InstanceOfOperator instanceOfOperator = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(new TypeParser(instanceOfOperator));
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

				// Parse a subscript access
				// e.g. this[1], array[0]

				final SubscriptAccess subscriptAccess = new SubscriptAccess(token, this.value);
				ArgumentListParser.parseArguments(pm, token.next(), subscriptAccess);
				this.value = subscriptAccess;
				this.mode = SUBSCRIPT_END;
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				// EXPRESSION (

				// Parse an apply call
				// e.g. 1("a"), this("stuff"), "myString"(2)

				final ApplyMethodCall applyMethodCall = new ApplyMethodCall(this.value.getPosition(), this.value);
				ArgumentListParser.parseArguments(pm, token.next(), applyMethodCall);

				this.value = applyMethodCall;
				this.mode = PARAMETERS_END;
				return;
			case BaseSymbols.COLON:
				this.parseInfixAccess(pm, token, Names.colon);
				return;
			case DyvilSymbols.ELLIPSIS:
				this.parseInfixAccess(pm, token, Names.dotdotdot);
				return;
			case BaseSymbols.EQUALS:
				if (this.value == null)
				{
					pm.report(Markers.syntaxError(token, "assignment.invalid", token));
					this.mode = VALUE;
					return;
				}

				this.parseInfixAccess(pm, token, Names.eq);
				return;
			}

			if (isSymbol(type))
			{
				this.parseInfixAccess(pm, token);
				return;
			}

			if (this.value != null)
			{
				// EXPRESSION EXPRESSION -> EXPRESSION ( EXPRESSION )

				if (this.hasFlag(IGNORE_APPLY) || this.ignoreClosure(token))
				{
					this.end(pm, true);
					return;
				}

				final ApplyMethodCall applyCall = new ApplyMethodCall(this.value.getPosition(), this.value,
				                                                      EmptyArguments.VISIBLE);

				this.value = applyCall;
				this.parseApply(pm, token, applyCall);
				pm.reparse();
				return;
			}
		}
		if (this.mode == DOT_ACCESS)
		{
			// EXPRESSION .

			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				// EXPRESSION . {

				final BraceAccessExpr braceAccessExpr = new BraceAccessExpr(token.raw(), this.value);
				pm.pushParser(new StatementListParser(braceAccessExpr::setStatement), true);
				this.value = braceAccessExpr;
				this.mode = ACCESS;
				return;
			}

			if (isIdentifier(type))
			{
				// EXPRESSION . IDENTIFIER

				this.parseInfixAccess(pm, token);
				return;
			}

			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
			}

			pm.report(Markers.syntaxError(token, "expression.dot.invalid"));
			return;
		}

		if (ParserUtil.isTerminator(type))
		{
			pm.popParser(true);
		}
		pm.report(Markers.syntaxError(token, "expression.invalid", token.toString()));
	}

	public boolean ignoreClosure(IToken token)
	{
		return token.type() == BaseSymbols.OPEN_CURLY_BRACKET && this.hasFlag(IGNORE_CLOSURE);
	}

	private void parseInfixAccess(IParserManager pm, IToken token)
	{
		this.parseInfixAccess(pm, token, token.nameValue());
	}

	private void parseInfixAccess(IParserManager pm, IToken token, Name name)
	{
		final int type = token.type();
		final IToken next = token.next();
		final int nextType = next.type();

		if (isSymbol(type))
		{
			// Identifier is an operator

			if (this.value == null) // prefix
			{
				// OPERATOR EXPRESSION
				// token    next

				final PrefixCall call = new PrefixCall(token.raw(), name);
				this.value = call;
				this.mode = ACCESS;

				if (isExpressionEnd(nextType))
				{
					pm.report(next, "expression.prefix.expression");
					return;
				}
				this.parseApply(pm, next, call);
				return;
			}
			if (isExpressionEnd(nextType) || isSymbol(nextType) && neighboring(token.prev(), token) && !neighboring(
				next, next.next()))
			{
				// EXPRESSION_OPERATOR EXPRESSION
				// EXPRESSION OPERATOR EOF
				//            token    next

				this.value = new PostfixCall(token.raw(), this.value, name);
				this.mode = ACCESS;
				return;
			}

			if (this.hasFlag(IGNORE_OPERATOR))
			{
				this.valueConsumer.setValue(this.value);
				pm.popParser(true);
				return;
			}

			// EXPRESSION OPERATOR EXPRESSION
			//            token    next

			final OperatorChain chain;

			if (this.value.valueTag() == IValue.OPERATOR_CHAIN)
			{
				chain = (OperatorChain) this.value;
			}
			else
			{
				chain = new OperatorChain();
				chain.addOperand(this.value);
				this.value = chain;
			}

			chain.addOperator(name, token.raw());
			pm.pushParser(new ExpressionParser(chain::addOperand).withFlag(this.flags | IGNORE_OPERATOR));
			return;
		}

		// Identifier is not an operator

		switch (nextType)
		{
		case BaseSymbols.OPEN_PARENTHESIS:
		{
			// IDENTIFIER (
			final MethodCall call = new MethodCall(token.raw(), this.value, name);
			ArgumentListParser.parseArguments(pm, next.next(), call);
			this.value = call;

			this.mode = PARAMETERS_END;
			pm.skip();
			return;
		}
		case BaseSymbols.OPEN_SQUARE_BRACKET:
		{
			// IDENTIFIER [

			final FieldAccess fieldAccess = new FieldAccess(token.raw(), this.value, name);
			final SubscriptAccess subscriptAccess = new SubscriptAccess(next.raw(), fieldAccess);
			ArgumentListParser.parseArguments(pm, next.next(), subscriptAccess);

			this.value = subscriptAccess;
			this.mode = SUBSCRIPT_END;
			pm.skip();
			return;
		}
		case DyvilSymbols.ARROW_RIGHT:
		case DyvilSymbols.DOUBLE_ARROW_RIGHT:
			if (this.hasFlag(IGNORE_LAMBDA))
			{
				break;
			}

			// IDENTIFIER =>   ...
			// token      next

			// Lambda Expression with one untyped parameter

			pm.pushParser(new LambdaOrTupleParser(this, LambdaOrTupleParser.SINGLE_PARAMETER), true);
			this.mode = END;
			return;
		}

		if (TypeParser.isGenericStart(next, nextType))
		{
			final IToken endToken = ParserUtil.findMatch(next, true);
			if (endToken != null && isTypeArgumentsEnd(endToken))
			{
				final MethodCall call = new MethodCall(token.raw(), this.value, name, EmptyArguments.INSTANCE);
				this.value = call;

				pm.splitJump(next, 1);
				pm.pushParser(new TypeListParser(call.getGenericData()));
				this.mode = TYPE_ARGUMENTS_END;
				return;
			}
		}

		if (this.parseFieldAccess(token, next, nextType))
		{
			this.value = new FieldAccess(token.raw(), this.value, name);
			this.mode = ACCESS;
			return;
		}

		// IDENTIFIER EXPRESSION
		// token      next

		// Parse a single-argument call
		// e.g. println "abc"
		//      println -1
		//      println i

		final MethodCall call = new MethodCall(token.raw(), this.value, name, EmptyArguments.INSTANCE);
		this.value = call;
		this.mode = ACCESS;

		this.parseApply(pm, token.next(), call);
	}

	private boolean parseFieldAccess(IToken token, IToken next, int nextType)
	{
		if (this.ignoreClosure(next))
		{
			return true;
		}
		if (isSymbol(nextType) && nextType != DyvilSymbols.UNDERSCORE)
		{
			// IDENTIFIER_SYMBOL ...
			// IDENTIFIER SYMBOL EXPRESSION

			return neighboring(token, next) || isExpressionEnd(next.next().type()) || !neighboring(next, next.next());
		}
		// IDENTIFIER END
		// token      next
		return isExpressionEnd(nextType) || this.hasFlag(IGNORE_APPLY);
	}

	private static boolean isTypeArgumentsEnd(IToken token)
	{
		final IToken next = token.next();
		final int nextType = next.type();

		if (isExpressionEnd(nextType))
		{
			return true;
		}
		switch (nextType)
		{
		case BaseSymbols.OPEN_CURLY_BRACKET:
			return true;
		case BaseSymbols.OPEN_PARENTHESIS:
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			return neighboring(token, next);
		}
		return false;
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
	 * 	the current parsing context manager
	 * @param token
	 * 	the first token of the expression that is a parameter to the APPLY method
	 * @param call
	 * 	the method or apply call
	 */
	private void parseApply(IParserManager pm, IToken token, ICall call)
	{
		if (token.type() != BaseSymbols.OPEN_CURLY_BRACKET)
		{
			final SingleArgument argument = new SingleArgument();
			call.setArguments(argument);
			pm.pushParser(new ExpressionParser(argument).withFlag(this.flags | IGNORE_APPLY | IGNORE_OPERATOR));
			return;
		}

		if (this.hasFlag(IGNORE_CLOSURE))
		{
			this.end(pm, false);
			return;
		}

		final SingleArgument argument = new SingleArgument();
		call.setArguments(argument);
		pm.pushParser(new StatementListParser(argument, true));
	}

	private boolean parseValue(IParserManager pm, IToken token, int type)
	{
		switch (type)
		{
		case Tokens.STRING:
		case Tokens.VERBATIM_STRING:
			this.value = new StringValue(token.raw(), token.stringValue());
			this.mode = ACCESS;
			return true;
		case Tokens.STRING_START:
		{
			final StringInterpolationExpr stringInterpolation = new StringInterpolationExpr(token);
			this.value = stringInterpolation;
			this.mode = ACCESS;
			pm.pushParser(new StingInterpolationParser(stringInterpolation), true);
			return true;
		}
		case Tokens.SINGLE_QUOTED_STRING:
			this.value = new CharValue(token.raw(), token.stringValue());
			this.mode = ACCESS;
			return true;
		case Tokens.VERBATIM_CHAR:
			this.value = new CharValue(token.raw(), token.stringValue(), true);
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
		case DyvilSymbols.UNDERSCORE:
			// _ ...
			this.value = new WildcardValue(token.raw());
			this.mode = ACCESS;
			return true;
		case BaseSymbols.OPEN_PARENTHESIS:
		{
			// ( ...
			final IToken next = token.next();

			if (next.type() != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.pushParser(new LambdaOrTupleParser(this, this.hasFlag(IGNORE_LAMBDA)), true);
				this.mode = ACCESS;
				return true;
			}

			if (!this.hasFlag(IGNORE_LAMBDA))
			{
				final IToken next2 = next.next();
				final int next2Type = next2.type();
				if (next2Type == DyvilSymbols.ARROW_RIGHT || next2Type == DyvilSymbols.DOUBLE_ARROW_RIGHT)
				{
					// () =>
					// () ->
					pm.skip();
					pm.pushParser(new LambdaOrTupleParser(this, LambdaOrTupleParser.TYPE_ARROW));
					this.mode = END;
					return true;
				}
			}

			// ()
			this.value = new VoidValue(token.to(token.next()));
			pm.skip();
			this.mode = ACCESS;
			return true;
		}
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			// [ ...
			this.mode = ACCESS;
			pm.pushParser(new ArrayLiteralParser(this), true);
			return true;
		case BaseSymbols.OPEN_CURLY_BRACKET:
			// { ...
			this.mode = ACCESS;
			pm.pushParser(new StatementListParser(this), true);
			return true;
		case DyvilSymbols.AT:
			// @ ...
			Annotation a = new Annotation();
			pm.pushParser(new AnnotationParser(a));
			this.value = new AnnotationValue(a);
			this.mode = END;
			return true;
		case DyvilSymbols.ARROW_RIGHT:
		case DyvilSymbols.DOUBLE_ARROW_RIGHT:
		{
			if (this.hasFlag(IGNORE_LAMBDA))
			{
				pm.popParser(true);
				return true;
			}

			// => ...
			// -> ...
			pm.pushParser(new LambdaOrTupleParser(this, LambdaOrTupleParser.TYPE_ARROW), true);
			return true;
		}
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
		case DyvilKeywords.INIT:
			// init ...
			this.mode = ACCESS;
			pm.pushParser(new ThisSuperInitParser(this), true);
			return true;
		case DyvilKeywords.THIS:
			// this ...
			this.mode = ACCESS;
			pm.pushParser(new ThisSuperInitParser(this, false));
			return true;
		case DyvilKeywords.SUPER:
			// super ...
			this.mode = ACCESS;
			pm.pushParser(new ThisSuperInitParser(this, true));
			return true;
		case DyvilKeywords.CLASS:
			// class ...
			this.mode = ACCESS;
			pm.pushParser(new TypeClassParser(this, token, true));
			return true;
		case DyvilKeywords.TYPE:
			// type ...
			this.mode = ACCESS;
			pm.pushParser(new TypeClassParser(this, token, false));
			return true;
		case DyvilKeywords.NEW:
			// new ...
			this.mode = ACCESS;
			pm.pushParser(new ConstructorCallParser(this), true);
			return true;
		case DyvilKeywords.RETURN:
		{
			// return ...

			ReturnStatement returnStatement = new ReturnStatement(token.raw());
			this.value = returnStatement;

			pm.pushParser(new ExpressionParser(returnStatement));
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

			this.end(pm, true);
			return true;
		}
		case DyvilKeywords.WHILE:
		{
			// while ...

			if (this.parent instanceof RepeatStatementParser // repeat parent
				    || this.parent instanceof ExpressionParser // repeat grandparent
					       && this.parent.getParent() instanceof RepeatStatementParser)
			{
				this.end(pm, true);
				return true;
			}

			final WhileStatement whileStatement = new WhileStatement(token);
			this.value = whileStatement;

			pm.pushParser(new WhileStatementParser(whileStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.REPEAT:
		{
			// repeat ...

			final RepeatStatement repeatStatement = new RepeatStatement(token);
			this.value = repeatStatement;

			pm.pushParser(new RepeatStatementParser(repeatStatement));
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
			if (isIdentifier(next.type()))
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
			if (isIdentifier(next.type()))
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
			if (isIdentifier(next.type()))
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

			this.end(pm, true);
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

			this.end(pm, true);
			return true;
		}
		case DyvilKeywords.THROW:
		{
			final ThrowStatement throwStatement = new ThrowStatement(token.raw());
			this.value = throwStatement;

			pm.pushParser(new ExpressionParser(throwStatement));
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

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
}
