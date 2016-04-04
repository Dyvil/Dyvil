package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.NestedMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.statement.Closure;
import dyvil.tools.compiler.ast.statement.FieldInitializer;
import dyvil.tools.compiler.ast.statement.MemberStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.TryParserManager;
import dyvil.tools.compiler.parser.classes.MemberParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.parsing.TryParserManager.EXIT_ON_ROOT;
import static dyvil.tools.compiler.parser.classes.MemberParser.*;
import static dyvil.tools.compiler.parser.method.ParameterListParser.LAMBDA_ARROW_END;

public final class StatementListParser extends Parser implements IValueConsumer, IMemberConsumer<IVariable>
{
	private static final int OPEN_BRACKET          = 0;
	private static final int LAMBDA_PARAMETERS_END = 1;
	private static final int LAMBDA_ARROW          = 2;
	private static final int EXPRESSION            = 4;
	private static final int SEPARATOR             = 8;

	protected IValueConsumer consumer;
	protected boolean        closure;

	private LambdaExpr    lambdaExpr;
	private StatementList statementList;

	private Name label;

	public StatementListParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		// this.mode = OPEN_BRACKET;
	}

	public StatementListParser(IValueConsumer consumer, boolean closure)
	{
		this.consumer = consumer;
		this.closure = closure;
		// this.mode = OPEN_BRACKET;
	}

	public void end(IParserManager pm)
	{
		this.consumer.setValue(this.lambdaExpr != null ? this.lambdaExpr : this.statementList);
		pm.popParser();
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			this.end(pm);
			return;
		}
		if (type == Tokens.EOF)
		{
			this.end(pm);
			pm.report(token, "statement_list.close_brace");
			return;
		}

		switch (this.mode)
		{
		case OPEN_BRACKET:
		{
			final IToken next = token.next();
			final IToken lambdaArrow = this.findLambdaArrow(next);
			if (lambdaArrow != null)
			{
				this.lambdaExpr = new LambdaExpr(lambdaArrow.raw());
				this.lambdaExpr.setValue(this.statementList = new StatementList(token));

				if (next.type() == BaseSymbols.OPEN_PARENTHESIS)
				{
					pm.skip();
					pm.pushParser(new ParameterListParser(this.lambdaExpr));
					this.mode = LAMBDA_PARAMETERS_END;
					return;
				}
				else
				{
					pm.pushParser(new ParameterListParser(this.lambdaExpr).withFlags(LAMBDA_ARROW_END));
					this.mode = LAMBDA_ARROW;
				}
				return;
			}

			this.statementList = this.closure ? new Closure(token) : new StatementList(token);
			this.mode = EXPRESSION;
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "statement_list.open_brace");
				pm.reparse();
			}
			return;
		}
		case LAMBDA_PARAMETERS_END:
			this.mode = LAMBDA_ARROW;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "statement_list.lambda.close_paren");
			}
			return;
		case LAMBDA_ARROW:
			if (type != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.report(token, "statement_list.lambda.arrow");
				return;
			}
			this.mode = EXPRESSION;
			return;
		case EXPRESSION:
			if (type == BaseSymbols.SEMICOLON || type == BaseSymbols.COMMA)
			{
				return;
			}

			if (ParserUtil.isIdentifier(type) && token.next().type() == BaseSymbols.COLON)
			{
				// IDENTIFIER : ...
				this.label = token.nameValue();
				pm.skip();
				// mode stays EXPRESSION
				return;
			}

			final TokenIterator tokens = pm.getTokens();

			// Have to rewind one token because the TryParserManager assumes the TokenIterator is at the beginning (i.e.
			// no tokens have been returned by next() yet)
			tokens.jump(token);
			final MemberParser parser = new MemberParser<>(this).withFlag(
				NO_UNINITIALIZED_VARIABLES | OPERATOR_ERROR | NO_FIELD_PROPERTIES);
			if (new TryParserManager(DyvilSymbols.INSTANCE, tokens, pm.getMarkers()).parse(parser, EXIT_ON_ROOT))
			{
				tokens.jump(tokens.lastReturned());
				this.mode = SEPARATOR;
				return;
			}

			// Reset to the current token
			tokens.jump(token);
			pm.pushParser(new ExpressionParser(this));
			return;
		case SEPARATOR:
			this.mode = EXPRESSION;
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			if (token.prev().type() == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.reparse();
				return;
			}
			pm.report(token, "statement_list.semicolon");
		}
	}

	private IToken findLambdaArrow(IToken token)
	{
		int parenDepth = 0;
		int bracketDepth = 0;
		int braceDepth = 0;

		for (; ; token = token.next())
		{
			final int type = token.type();
			switch (type)
			{
			case BaseSymbols.OPEN_PARENTHESIS:
				parenDepth++;
				continue;
			case BaseSymbols.CLOSE_PARENTHESIS:
				if (parenDepth < 0)
				{
					return null;
				}
				parenDepth--;
				continue;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				bracketDepth++;
				continue;
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
				if (bracketDepth < 0)
				{
					return null;
				}
				bracketDepth--;
				continue;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				braceDepth++;
				continue;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				if (braceDepth < 0)
				{
					return null;
				}
				braceDepth--;
				continue;
			case BaseSymbols.EQUALS:
				if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0)
				{
					return null;
				}
				continue;
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
				if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0)
				{
					return token;
				}
				continue;
			case BaseSymbols.SEMICOLON:
			case Tokens.EOF:
				return null;
			}
		}
	}

	@Override
	public void setValue(IValue value)
	{
		if (this.label != null)
		{
			this.statementList.addValue(value, new Label(this.label, value));
			this.label = null;
		}
		else
		{
			this.statementList.addValue(value);
		}
	}

	@Override
	public void addDataMember(IVariable field)
	{
		this.setValue(new FieldInitializer(field));
	}

	@Override
	public IVariable createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}

	@Override
	public void addMethod(IMethod method)
	{
		this.setValue(new MemberStatement(method));
	}

	@Override
	public IMethod createMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new NestedMethod(position, name, type, modifiers, annotations);
	}

	@Override
	public void addProperty(IProperty property)
	{
		this.setValue(new MemberStatement(property));
	}

	@Override
	public void addConstructor(IConstructor constructor)
	{
		this.setValue(new MemberStatement(constructor));
	}

	@Override
	public void addInitializer(IInitializer initializer)
	{
		this.setValue(new MemberStatement(initializer));
	}

	@Override
	public void addClass(IClass theClass)
	{
		this.setValue(new MemberStatement(theClass));
	}
}
