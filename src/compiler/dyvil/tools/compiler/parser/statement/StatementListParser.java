package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
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
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.TryParserManager;
import dyvil.tools.compiler.parser.classes.MemberParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.TryParserManager.EXIT_ON_ROOT;
import static dyvil.tools.compiler.parser.classes.MemberParser.NO_UNINITIALIZED_VARIABLES;
import static dyvil.tools.compiler.parser.classes.MemberParser.OPERATOR_ERROR;

public final class StatementListParser extends Parser implements IValueConsumer, IMemberConsumer<IVariable>
{
	private static final int OPEN_BRACKET = 1;
	private static final int EXPRESSION   = 2;
	private static final int SEPARATOR    = 4;

	protected IValueConsumer consumer;
	protected boolean        closure;

	private StatementList statementList;

	private Name label;

	public StatementListParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = OPEN_BRACKET;
	}

	public StatementListParser(IValueConsumer consumer, boolean closure)
	{
		this.consumer = consumer;
		this.closure = closure;
		this.mode = OPEN_BRACKET;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			this.consumer.setValue(this.statementList);
			pm.popParser();
			return;
		}
		if (type == Tokens.EOF)
		{
			this.consumer.setValue(this.statementList);
			pm.popParser();
			pm.report(token, "statementlist.close_brace");
			return;
		}

		switch (this.mode)
		{
		case OPEN_BRACKET:
			this.mode = EXPRESSION;
			this.statementList = this.closure ? new Closure(token) : new StatementList(token);
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "statementlist.close_brace");
				pm.reparse();
			}
			return;
		case EXPRESSION:
			if (type == BaseSymbols.SEMICOLON)
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
			final MemberParser parser = new MemberParser(this).withFlag(NO_UNINITIALIZED_VARIABLES | OPERATOR_ERROR);
			if (new TryParserManager(tokens, pm.getMarkers(), pm.getOperatorMap()).parse(parser, EXIT_ON_ROOT))
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
			pm.report(token, "statementlist.semicolon");
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
