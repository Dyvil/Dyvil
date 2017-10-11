package dyvilx.tools.compiler.parser.statement;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.consumer.IMemberConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LambdaExpr;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.NestedMethod;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.statement.Closure;
import dyvilx.tools.compiler.ast.statement.MemberStatement;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.statement.VariableStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.classes.MemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.expression.LambdaOrTupleParser;
import dyvilx.tools.compiler.parser.method.ParameterListParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.TryParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.method.ParameterListParser.LAMBDA_ARROW_END;
import static dyvilx.tools.parsing.TryParserManager.EXIT_ON_ROOT;

public final class StatementListParser extends Parser implements IValueConsumer, IMemberConsumer<IVariable>
{
	private static final int OPEN_BRACKET          = 0;
	private static final int LAMBDA_PARAMETERS_END = 1;
	private static final int LAMBDA_TYPE_ARROW     = 1 << 1;
	private static final int LAMBDA_RETURN_ARROW   = 1 << 2;
	private static final int EXPRESSION            = 1 << 3;
	private static final int LABEL_NAME            = 1 << 4;
	private static final int LABEL_END             = 1 << 5;
	private static final int SEPARATOR             = 1 << 6;

	private final TryParserManager tryParserManager = new TryParserManager(DyvilSymbols.INSTANCE);

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

				if (next == lambdaArrow)
				{
					// { ->
					// { =>
					this.mode = LAMBDA_TYPE_ARROW;
					return;
				}
				if (next.type() == BaseSymbols.OPEN_PARENTHESIS)
				{
					// { ( ... ) =>
					// { ( ... ) ->
					pm.skip();
					pm.pushParser(new ParameterListParser(this.lambdaExpr));
					this.mode = LAMBDA_PARAMETERS_END;
					return;
				}

				// { ... ->
				// { ... =>
				pm.pushParser(new ParameterListParser(this.lambdaExpr).withFlags(LAMBDA_ARROW_END));
				this.mode = LAMBDA_TYPE_ARROW;
				return;
			}

			// { ...
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
			this.mode = LAMBDA_TYPE_ARROW;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "statement_list.lambda.close_paren");
			}
			return;
		case LAMBDA_TYPE_ARROW:
			if (type == DyvilSymbols.ARROW_RIGHT)
			{
				pm.pushParser(LambdaOrTupleParser.returnTypeParser(this.lambdaExpr));
				this.mode = LAMBDA_RETURN_ARROW;
				return;
			}
			// Fallthrough
		case LAMBDA_RETURN_ARROW:
			if (type != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.report(token, "statement_list.lambda.arrow");
				return;
			}
			this.mode = EXPRESSION;
			return;
		case EXPRESSION:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.COMMA:
				return;
			case DyvilKeywords.LABEL:
				this.mode = LABEL_NAME;
				return;
			}

			this.mode = SEPARATOR;
			final MemberParser<IVariable> parser = new MemberParser<>(this).withFlags(MemberParser.NO_FIELD_PROPERTIES);
			if (this.tryParserManager.tryParse(pm, parser, token, EXIT_ON_ROOT))
			{
				return;
			}

			pm.pushParser(new ExpressionParser(this));
			return;
		case LABEL_NAME:
			if (Tokens.isIdentifier(type))
			{
				this.label = token.nameValue();
				this.mode = LABEL_END;
				return;
			}
			this.mode = EXPRESSION;
			if (type != BaseSymbols.COLON)
			{
				pm.reparse();
			}
			pm.report(token, "statement_list.label.name");
			return;
		case LABEL_END:
			switch (type)
			{
			case BaseSymbols.COLON:
			case BaseSymbols.SEMICOLON:
				this.mode = EXPRESSION;
				return;
			}
			this.mode = EXPRESSION;
			pm.reparse();
			pm.report(SourcePosition.between(token, token.next()), "statement_list.label.separator");
			return;
		case SEPARATOR:
			this.mode = EXPRESSION;
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.COMMA:
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
			case DyvilKeywords.LET:
			case DyvilKeywords.VAR:
			case DyvilKeywords.FUNC:
				if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0)
				{
					return null;
				}
				continue;
			case DyvilSymbols.ARROW_RIGHT:
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
			this.statementList.add(this.label, value);
			this.label = null;
		}
		else
		{
			this.statementList.add(value);
		}
	}

	@Override
	public void addDataMember(IVariable field)
	{
		this.setValue(new VariableStatement(field));
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                 AttributeList annotations)
	{
		return new Variable(position, name, type, modifiers, annotations);
	}

	@Override
	public void addMethod(IMethod method)
	{
		this.setValue(new MemberStatement(method));
	}

	@Override
	public IMethod createMethod(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                           AttributeList annotations)
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
