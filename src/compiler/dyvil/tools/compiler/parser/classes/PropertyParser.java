package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PropertyParser extends Parser implements IValueConsumer
{
	private static final int TAG                   = 0;
	private static final int SEPARATOR             = 1;
	private static final int SETTER_PARAMETER      = 2;
	private static final int SETTER_PARAMETER_NAME = 4;
	private static final int SETTER_PARAMETER_END  = 8;
	
	protected Property property;

	private ModifierSet modifiers;
	private boolean     targetSetter;

	public PropertyParser(Property property)
	{
		this.property = property;
		// this.mode = TAG; // pointless assignment to 0
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();

		switch (this.mode)
		{
		case TAG:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				return;
			}

			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}
			
			Modifier modifier;
			if ((modifier = BaseModifiers.parseModifier(token, pm)) != null)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addModifier(modifier);
				return;
			}
			
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == Names.get)
				{
					this.property.setGetterPosition(token.raw());
					this.property.setGetterModifiers(this.modifiers);
					this.modifiers = null;
					this.mode = SEPARATOR;
					this.targetSetter = false;
					return;
				}
				if (name == Names.set)
				{
					this.property.setSetterPosition(token.raw());
					this.property.setSetterModifiers(this.modifiers);
					this.modifiers = null;
					this.mode = SETTER_PARAMETER;
					this.targetSetter = true;
					return;
				}
				pm.report(token, "property.tag.unknown");
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
			if (type == BaseSymbols.COLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new StatementListParser(this), true);
				return;
			}
			if (type == BaseSymbols.SEMICOLON)
			{
				this.mode = TAG;
				return;
			}
			pm.report(token, "property.colon");
			return;
		case SETTER_PARAMETER_NAME:
			this.mode = SETTER_PARAMETER_END;
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "property.setter.identifier");
			}
			else
			{
				this.property.setSetterParameterName(token.nameValue());
			}
			return;
		case SETTER_PARAMETER_END:
			this.mode = SEPARATOR;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "property.setter.close_paren");
			}
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.targetSetter)
		{
			this.property.setSetterValue(value);
		}
		else
		{
			this.property.setGetterValue(value);
		}
	}
}
