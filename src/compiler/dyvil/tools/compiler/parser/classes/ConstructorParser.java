package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class ConstructorParser extends AbstractMemberParser
{
	protected static final int DECLARATOR     = 0;
	protected static final int PARAMETERS     = 1;
	protected static final int PARAMETERS_END = 2;
	protected static final int EXCEPTIONS     = 3;
	protected static final int BODY           = 4;

	protected final IMemberConsumer<?> consumer;

	private IConstructor member;

	public ConstructorParser(IMemberConsumer<?> consumer)
	{
		this.consumer = consumer;
		this.mode = DECLARATOR;
	}

	public ConstructorParser(IMemberConsumer<?> consumer, ModifierSet modifiers, AnnotationList annotations)
	{
		this.consumer = consumer;
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.mode = DECLARATOR;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			switch (type)
			{
			case DyvilSymbols.AT:
				this.parseAnnotation(pm, token);
				return;
			case DyvilKeywords.INIT:
				this.member = this.consumer.createConstructor(token.raw(), this.modifiers, this.annotations);
				this.mode = PARAMETERS;
				return;
			}

			if (this.parseModifier(pm, token))
			{
				return;
			}

			pm.report(token, "member.declarator");
			return;
		case PARAMETERS:
			this.mode = PARAMETERS_END;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser(this.member));
				return;
			}
			pm.reparse();
			pm.report(token, "constructor.parameters.open_paren");
			return;
		case PARAMETERS_END:
			this.mode = EXCEPTIONS;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "constructor.parameters.close_paren");
			}
			return;
		case EXCEPTIONS:
			if (type == DyvilKeywords.THROWS)
			{
				pm.pushParser(new TypeListParser(this.member.getExceptions()));
				this.mode = BODY;
				return;
			}
			// Fallthrough
		case BODY:
			switch (type)
			{
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.pushParser(new StatementListParser(this.member), true);
				this.mode = END;
				return;
			case BaseSymbols.EQUALS:
				pm.pushParser(new ExpressionParser(this.member));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			this.consumer.addConstructor(this.member);
			pm.popParser(type != Tokens.EOF);
		}
	}
}
