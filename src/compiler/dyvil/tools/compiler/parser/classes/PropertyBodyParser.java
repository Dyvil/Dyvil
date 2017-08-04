package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PropertyBodyParser extends AbstractMemberParser implements IValueConsumer
{
	// Modes
	private static final int OPEN_BRACE            = 0;
	private static final int TAG                   = 1;
	private static final int SEPARATOR             = 2;
	private static final int SETTER_PARAMETER      = 3;
	private static final int SETTER_PARAMETER_NAME = 4;
	private static final int SETTER_PARAMETER_END  = 5;

	// Targets
	private static final byte GETTER      = 0;
	private static final byte SETTER      = 1;
	private static final byte INITIALIZER = 2;

	// --------------------------------------------------

	protected IProperty property;

	// Metadata
	private byte target;

	public PropertyBodyParser(IProperty property)
	{
		this.property = property;
		// this.mode = OPEN_BRACE;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		switch (this.mode)
		{
		case OPEN_BRACE:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.mode = TAG;
			}
			return;
		case TAG:
			switch (type)
			{
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
				break;
			case Tokens.LETTER_IDENTIFIER:
				final Name name = token.nameValue();
				if (name == Names.get)
				{
					this.configureMethod(this.property.initGetter(), token);
					this.mode = SEPARATOR;
					this.target = GETTER;
					return;
				}
				if (name == Names.set)
				{
					this.configureMethod(this.property.initSetter(), token);
					this.mode = SETTER_PARAMETER;
					this.target = SETTER;
					return;
				}
				pm.report(token, "property.tag.unknown");
				return;
			case DyvilKeywords.INIT:
				this.property.setInitializerPosition(token.raw());
				this.mode = SEPARATOR;
				this.target = INITIALIZER;
				return;
			case DyvilSymbols.AT:
				this.parseAnnotation(pm, token);
				return;
			}

			if (this.parseModifier(pm, token))
			{
				return;
			}

			pm.report(token, "property.tag");
			return;
		case SETTER_PARAMETER:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = SETTER_PARAMETER_NAME;
				return;
			}
			// Fallthrough
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.COLON:
				pm.pushParser(new ExpressionParser(this));
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.pushParser(new StatementListParser(this), true);
				return;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				this.mode = TAG;
				return;
			}
			pm.report(token, "property.separator");
			return;
		case SETTER_PARAMETER_NAME:
			this.mode = SETTER_PARAMETER_END;
			if (ParserUtil.isIdentifier(type))
			{
				this.property.setSetterParameterName(token.nameValue());
			}
			else
			{
				pm.report(token, "property.setter.identifier");
			}
			return;
		case SETTER_PARAMETER_END:
			this.mode = SEPARATOR;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "property.setter.close_paren");
			}
		}
	}

	private void configureMethod(IMethod method, IToken token)
	{
		method.setPosition(token.raw());
		if (this.modifiers != null)
		{
			method.setModifiers(this.modifiers);
			this.modifiers = null;
		}
		if (this.annotations != null)
		{
			method.setAnnotations(this.annotations);
			this.annotations = null;
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.target)
		{
		case GETTER:
			this.property.initGetter().setValue(value);
			return;
		case SETTER:
			this.property.initSetter().setValue(value);
			return;
		case INITIALIZER:
			this.property.setInitializer(value);
		}
	}
}
