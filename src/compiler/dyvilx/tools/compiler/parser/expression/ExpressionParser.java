package dyvilx.tools.compiler.parser.expression;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.*;
import dyvilx.tools.compiler.ast.expression.access.*;
import dyvilx.tools.compiler.ast.expression.constant.*;
import dyvilx.tools.compiler.ast.expression.operator.InfixCallChain;
import dyvilx.tools.compiler.ast.expression.operator.PostfixCall;
import dyvilx.tools.compiler.ast.expression.operator.PrefixCall;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.statement.ReturnStatement;
import dyvilx.tools.compiler.ast.statement.SyncStatement;
import dyvilx.tools.compiler.ast.statement.control.BreakStatement;
import dyvilx.tools.compiler.ast.statement.control.ContinueStatement;
import dyvilx.tools.compiler.ast.statement.control.GoToStatement;
import dyvilx.tools.compiler.ast.statement.exception.ThrowStatement;
import dyvilx.tools.compiler.ast.statement.exception.TryStatement;
import dyvilx.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvilx.tools.compiler.ast.statement.loop.WhileStatement;
import dyvilx.tools.compiler.parser.BracketMatcher;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.statement.*;
import dyvilx.tools.compiler.parser.type.TypeListParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.parsing.lexer.Tokens.isSymbolic;

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

	public static final  int IGNORE_APPLY    = 0b00001;
	private static final int IGNORE_OPERATOR = 0b00010;
	public static final  int IGNORE_COLON    = 0b00100;
	public static final  int IGNORE_LAMBDA   = 0b01000;
	public static final  int IGNORE_CLOSURE  = 0b10000;

	public static final int IGNORE_STATEMENT = IGNORE_APPLY | IGNORE_COLON | IGNORE_CLOSURE;

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

	public void addFlags(int flag)
	{
		this.flags |= flag;
	}

	public ExpressionParser withFlags(int flag)
	{
		this.flags |= flag;
		return this;
	}

	public void removeFlags(int flag)
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
				// IDENTIFIER
				this.parseInfixAccess(pm, token, token.nameValue());
				return;
			}
			if (this.parseValue(pm, token, type))
			{
				// KEYWORD
				// LITERAL
				// STATEMENT
				return;
			}
			// Fallthrough
		case ACCESS:
			switch (type)
			{
			case BaseSymbols.DOT:
				this.mode = DOT_ACCESS;
				return;
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

				final IToken next = token.next();
				final boolean optional;
				if (next.type() == Tokens.SYMBOL_IDENTIFIER && next.nameValue() == Names.qmark && token.isNeighboring(next))
				{
					// EXPRESSION as?
					optional = true;
					pm.skip();
				}
				else
				{
					optional = false;
				}


				final CastOperator castOperator = new CastOperator(token.raw(), this.value, optional);
				pm.pushParser(new TypeParser(castOperator).withFlags(TypeParser.IGNORE_OPERATOR));
				this.value = castOperator;
				return;
			}
			case DyvilKeywords.IS:
			{
				// EXPRESSION is

				final InstanceOfOperator instanceOfOperator = new InstanceOfOperator(token.raw(), this.value);
				pm.pushParser(new TypeParser(instanceOfOperator).withFlags(TypeParser.IGNORE_OPERATOR));
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

				final ApplyAccess applyAccess = new ApplyAccess(this.value.getPosition(), this.value);
				ArgumentListParser.parseArguments(pm, token.next(), applyAccess);

				this.value = applyAccess;
				this.mode = PARAMETERS_END;
				return;
			case BaseSymbols.COLON:
				if (this.hasFlag(IGNORE_COLON))
				{
					this.end(pm, true);
					return;
				}

				this.parseInfixAccess(pm, token, Names.colon, true);
				return;
			case BaseSymbols.EQUALS:
				this.parseInfixAccess(pm, token, Names.eq, true);
				return;
			}

			if (isExpressionEnd(type))
			{
				// ... ]

				// Close bracket, end expression
				this.end(pm, true);
				return;
			}

			if (isSymbolic(type))
			{
				// EXPRESSION IDENTIFIER
				// EXPRESSION SYMBOL

				this.parseInfixAccess(pm, token);
				return;
			}

			if (this.value != null)
			{
				// EXPRESSION EXPRESSION -> EXPRESSION ( EXPRESSION )
				// Juxtaposition

				this.parseApply(pm, token);
				return;
			}

			pm.report(Markers.syntaxError(token, "expression.invalid", token.toString()));
			return;
		case DOT_ACCESS:
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

			if (Tokens.isIdentifier(type))
			{
				if (this.value == null)
				{
					// .IDENTIFIER
					this.value = new EnumValue(token.raw(), token.nameValue());
					this.mode = ACCESS;
					return;
				}

				// EXPRESSION . IDENTIFIER

				this.parseInfixAccess(pm, token, token.nameValue());
				return;
			}

			pm.report(Markers.syntaxError(token, "expression.access.dot.invalid"));
			if (BaseSymbols.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}

			this.mode = ACCESS;
			pm.reparse();
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
		case TYPE_ARGUMENTS_END:
			if (!TypeParser.isGenericEnd(token, type))
			{
				pm.reparse();
				pm.report(token, "method.call.generic.close_angle");
			}

			pm.splitJump(token, 1);
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
		throw new Error("unreachable");
	}

	private boolean ignoreClosure(int nextType)
	{
		return nextType == BaseSymbols.OPEN_CURLY_BRACKET && this.hasFlag(IGNORE_CLOSURE);
	}

	private boolean isOperatorEnd(int nextType)
	{
		return isExpressionEnd(nextType) || this.ignoreClosure(nextType);
	}

	private void parseInfixAccess(IParserManager pm, IToken token)
	{
		Name name = token.nameValue();
		if (name == null)
		{
			name = Name.from(token.stringValue());
		}
		this.parseInfixAccess(pm, token, name, false);
	}

	private void parseInfixAccess(IParserManager pm, IToken token, Name name)
	{
		this.parseInfixAccess(pm, token, name, false);
	}

	private void parseInfixAccess(IParserManager pm, IToken token, Name name, boolean forceInfix)
	{
		final int type = token.type();
		final IToken next = token.next();
		final int nextType = next.type();

		if (isSymbolic(type))
		{
			// Identifier is an operator
			final IToken prev = token.prev();
			final boolean leftNeighbor = prev.isNeighboring(token);
			final boolean rightNeighbor = token.isNeighboring(next);

			if (this.value == null) // prefix
			{
				if (forceInfix) // only true iff this.value == null
				{
					pm.report(SourcePosition.before(token), "expression.infix.before");
				}

				// OPERATOR EXPRESSION
				// token    next

				this.mode = ACCESS;
				if (this.isOperatorEnd(nextType))
				{
					this.value = new FieldAccess(token.raw(), null, name);
					return;
				}

				final PrefixCall call = new PrefixCall(token.raw(), name);
				this.value = call;
				this.parseApply(pm, next, call);
				return;
			}
			else if (!forceInfix && !leftNeighbor && rightNeighbor)
			{
				// Revert to Juxtaposition

				this.parseApply(pm, token);
				return;
			}
			if (this.isOperatorEnd(nextType) || !forceInfix && leftNeighbor && !rightNeighbor)
			{
				if (forceInfix) // only true iff this.isOperatorEnd(nextType)
				{
					pm.report(SourcePosition.after(token), "expression.infix.after");
				}

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

			final InfixCallChain chain;

			if (this.value.valueTag() == IValue.OPERATOR_CHAIN)
			{
				chain = (InfixCallChain) this.value;
			}
			else
			{
				chain = new InfixCallChain();
				chain.addOperand(this.value);
				this.value = chain;
			}

			chain.addOperator(name, token.raw());
			pm.pushParser(new ExpressionParser(chain::addOperand).withFlags(this.flags | IGNORE_OPERATOR));
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

		if (isGenericCall(next, nextType))
		{
			final MethodCall call = new MethodCall(token.raw(), this.value, name, ArgumentList.EMPTY);
			this.value = call;

			pm.splitJump(next, 1);
			pm.pushParser(new TypeListParser(call.getGenericData().getTypes(), true));
			this.mode = TYPE_ARGUMENTS_END;
			return;
		}

		if (this.isFieldAccess(token, next, nextType))
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

		final MethodCall call = new MethodCall(token.raw(), this.value, name, ArgumentList.empty());
		this.value = call;
		this.mode = ACCESS;

		this.parseApply(pm, token.next(), call);
	}

	private boolean isFieldAccess(IToken token, IToken next, int nextType)
	{
		if (this.hasFlag(IGNORE_APPLY) || this.ignoreClosure(nextType))
		{
			return true;
		}
		if (isSymbolic(nextType) && nextType != DyvilSymbols.UNDERSCORE)
		{
			// IDENTIFIER_SYMBOL ...
			// IDENTIFIER SYMBOL EXPRESSION

			return token.isNeighboring(next) || isExpressionEnd(next.next().type()) || !next.isNeighboring(next.next());
		}
		// IDENTIFIER END
		// token      next
		return isExpressionEnd(nextType);
	}

	public static boolean isGenericCall(IToken token, int tokenType)
	{
		if (!TypeParser.isGenericStart(token, tokenType))
		{
			// Identifier not followed by an opening angle bracket
			// IDENTIFIER
			return false;
		}

		// IDENTIFIER <

		final IToken endToken = BracketMatcher.findMatch(token, true);
		if (endToken == null)
		{
			// No closing angle bracket found
			// IDENTIFIER < ...
			return false;
		}

		final IToken endTokenNext = endToken.next();
		final int endTokenNextType = endTokenNext.type();

		// IDENTIFIER < ... >
		// IDENTIFIER < ... >SYMBOL
		// IDENTIFIER < ... SYMBOL>
		// IDENTIFIER < ... SYMBOL>SYMBOL

		if (!TypeParser.isGenericEnd2(endToken))
		{
			// The end token ends with more symbols
			// IDENTIFIER < ... SYMBOL>SYMBOL
			// IDENTIFIER < ... >SYMBOL

			// Return true iff the end token and the next token are NOT separated by whitespace
			return endToken.isNeighboring(endTokenNext);
		}

		// IDENTIFIER < ... >
		// IDENTIFIER < ... SYMBOL>

		// Check the token after the end token

		if (isExpressionEnd(endTokenNextType))
		{
			// The end token is followed by a token that ends an expression (e.g. ',', ';', ')', ']', '}')
			// IDENTIFIER < ... > END
			// IDENTIFIER < ... SYMBOL> END
			return true;
		}
		switch (endTokenNextType)
		{
		case Tokens.SYMBOL:
		case Tokens.SYMBOL_IDENTIFIER:
			// The end token is followed by another symbol token, but they are separated by whitespace
			// IDENTIFIER < ... > SYMBOL
			// IDENTIFIER < ... SYMBOL> SYMBOL

			// Return true iff the symbol token is either followed by a token that ends an expression
			// or separated from the next token via whitespace.
			final IToken endTokenNextNext = endTokenNext.next();
			return isExpressionEnd(endTokenNextNext.type()) || !endTokenNext.isNeighboring(endTokenNextNext);
		case BaseSymbols.OPEN_CURLY_BRACKET:
			// IDENTIFIER < ... > {
			return true;
		case BaseSymbols.OPEN_PARENTHESIS:
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			// The end token is followed by an opening or closing parentheses.
			// Return true iff they are NOT separated by whitespace
			// IDENTIFIER < ... >(
			// IDENTIFIER < ... >[
			return endToken.isNeighboring(endTokenNext);
		}
		return false;
	}

	private void parseApply(IParserManager pm, IToken token)
	{
		if (this.hasFlag(IGNORE_APPLY) || this.ignoreClosure(token.type()))
		{
			this.end(pm, true);
			return;
		}

		final ApplyAccess applyCall = new ApplyAccess(SourcePosition.between(token.prev(), token), this.value,
		                                              ArgumentList.empty());

		this.value = applyCall;
		this.parseApply(pm, token, applyCall);
		pm.reparse();
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
			final ArgumentList arguments = new ArgumentList(1);
			call.setArguments(arguments);
			pm.pushParser(new ExpressionParser(arguments).withFlags(this.flags | IGNORE_APPLY | IGNORE_OPERATOR));
			return;
		}

		if (this.hasFlag(IGNORE_CLOSURE))
		{
			this.end(pm, false);
			return;
		}

		final ArgumentList arguments = new ArgumentList(1);
		call.setArguments(arguments);
		pm.pushParser(new StatementListParser(arguments, true));
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
			Annotation a = new CodeAnnotation(token.raw());
			pm.pushParser(new AnnotationParser(a));
			this.value = new AnnotationExpr(a);
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
			pm.pushParser(new ThisSuperParser(this), true);
			return true;
		case DyvilKeywords.THIS:
			// this ...
			this.mode = ACCESS;
			pm.pushParser(new ThisSuperParser(this), true);
			return true;
		case DyvilKeywords.SUPER:
			// super ...
			this.mode = ACCESS;
			pm.pushParser(new ThisSuperParser(this), true);
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
			final int flags = this.hasFlag(IGNORE_CLOSURE) ? ConstructorCallParser.IGNORE_ANON_CLASS : 0;
			pm.pushParser(new ConstructorCallParser(this).withFlags(flags), true);
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
			pm.pushParser(new IfStatementParser(this), true);
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
		case DyvilKeywords.MATCH:
		{
			// match ...
			final MatchExpr matchExpr = new MatchExpr(token.raw());
			this.value = matchExpr;

			pm.pushParser(new MatchExpressionParser(matchExpr));
			this.mode = END;
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

			final WhileStatement whileStatement = new WhileStatement(token.raw());
			this.value = whileStatement;

			pm.pushParser(new WhileStatementParser(whileStatement));
			this.mode = END;
			return true;
		}
		case DyvilKeywords.REPEAT:
		{
			// repeat ...

			final RepeatStatement repeatStatement = new RepeatStatement(token.raw());
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
			if (Tokens.isIdentifier(next.type()))
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
			if (Tokens.isIdentifier(next.type()))
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
			if (Tokens.isIdentifier(next.type()))
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

	public static boolean isExpressionEnd(int type)
	{
		if (BaseSymbols.isTerminator(type))
		{
			return true;
		}
		switch (type)
		{
		case BaseSymbols.DOT:
		case BaseSymbols.EQUALS:
		case DyvilKeywords.IS:
		case DyvilKeywords.AS:
		case DyvilKeywords.MATCH:
		case DyvilKeywords.ELSE:
		case DyvilKeywords.FINALLY:
		case DyvilKeywords.CATCH:
		case Tokens.STRING_PART:
		case Tokens.STRING_END:
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
