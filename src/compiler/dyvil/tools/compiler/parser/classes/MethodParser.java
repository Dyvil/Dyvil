package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParameterListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

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

	protected final IMemberConsumer<?> consumer;

	private IMethod method;

	public MethodParser(IMemberConsumer<?> consumer)
	{
		this.consumer = consumer;
		this.modifiers = new ModifierList();
		this.mode = DECLARATOR;
	}

	public MethodParser(IMemberConsumer<?> consumer, ModifierSet modifiers, AnnotationList annotations)
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
			case DyvilKeywords.FUNC:
			case DyvilKeywords.OPERATOR:
				this.mode = METHOD_NAME;
				return;
			}

			if (this.parseModifier(pm, token))
			{
				return;
			}

			pm.report(token, "member.declarator");
			return;
		case METHOD_NAME:
		{
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "method.identifier");
				return;
			}

			this.method = this.consumer.createMethod(token.raw(), token.nameValue(), Types.UNKNOWN, this.modifiers,
			                                         this.annotations);

			this.mode = GENERICS;
			return;
		}
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
				pm.pushParser(new TypeParser(this.method));
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
