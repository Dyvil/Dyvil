package dyvilx.tools.compiler.parser.classes;

import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IMethodConsumer;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.method.ParameterListParser;
import dyvilx.tools.compiler.parser.statement.StatementListParser;
import dyvilx.tools.compiler.parser.type.TypeListParser;
import dyvilx.tools.compiler.parser.type.TypeParameterListParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class MethodParser extends AbstractMemberParser
{
	protected static final int DECLARATOR     = 0;
	protected static final int METHOD_NAME    = 1;
	protected static final int PARAMETERS     = 2;
	protected static final int PARAMETERS_END = 3;
	protected static final int GENERICS       = 4;
	protected static final int GENERICS_END   = 5;
	protected static final int TYPE           = 6;
	protected static final int EXCEPTIONS     = 7;
	protected static final int BODY           = 8;

	protected final IMethodConsumer consumer;

	private IMethod method;

	public MethodParser(IMethodConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = DECLARATOR;
	}

	public MethodParser(IMethodConsumer consumer, AttributeList attributes)
	{
		super(attributes);
		this.consumer = consumer;
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
			case DyvilKeywords.FUNC:
			case DyvilKeywords.OPERATOR:
				this.mode = METHOD_NAME;
				return;
			}

			if (this.parseAttribute(pm, token))
			{
				return;
			}

			// Fallthrough
		case METHOD_NAME:
			if (!Tokens.isIdentifier(type))
			{
				pm.report(token, "method.identifier");
				return;
			}

			this.method = this.consumer.createMethod(token.raw(), token.nameValue(), Types.UNKNOWN, this.attributes);

			this.mode = GENERICS;
			return;
		// Fallthrough
		case GENERICS:
			if (TypeParser.isGenericStart(token, type))
			{
				pm.splitJump(token, 1);
				this.mode = GENERICS_END;
				pm.pushParser(new TypeParameterListParser(this.method));
				return;
			}
			// Fallthrough
		case PARAMETERS:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = PARAMETERS_END;
				pm.pushParser(new ParameterListParser(this.method));
				return;
			}
			// Fallthrough
		case TYPE:
			switch (type)
			{
			case BaseSymbols.COLON:
				pm.report(Markers.syntaxWarning(token, "method.type.colon.deprecated"));
				// Fallthrough
			case DyvilSymbols.ARROW_RIGHT:
				pm.pushParser(new TypeParser(this.method::setType));
				this.mode = EXCEPTIONS;
				return;
			}
			// Fallthrough
		case EXCEPTIONS:
			if (type == DyvilKeywords.THROWS)
			{
				pm.pushParser(new TypeListParser(this.method.getExceptions()));
				this.mode = BODY;
				return;
			}
			// Fallthrough
		case BODY:
			if (this.parseAttribute(pm, token))
			{
				return;
			}

			switch (type)
			{
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.pushParser(new StatementListParser(this.method), true);
				this.mode = END;
				return;
			case BaseSymbols.EQUALS:
				pm.pushParser(new ExpressionParser(this.method));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			this.consumer.addMethod(this.method);
			pm.popParser(type != Tokens.EOF);
			return;
		case GENERICS_END:
			this.mode = PARAMETERS;
			if (TypeParser.isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.reparse();
			pm.report(token, "generic.close_angle");
			return;
		case PARAMETERS_END:
			this.mode = TYPE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "method.parameters.close_paren");
			}
			return;
		}
	}
}
