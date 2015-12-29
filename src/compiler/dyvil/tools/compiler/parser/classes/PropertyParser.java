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
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PropertyParser extends Parser implements IValueConsumer
{
	private static final int GET_OR_SET                = 1;
	private static final int COLON                     = 2;
	private static final int SET                       = 4;
	private static final int SETTER_PARAMETER_NAME     = 8;
	private static final int SETTER_PARAMETER_NAME_END = 16;
	
	public static final Name get = Name.getQualified("get");
	public static final Name set = Name.getQualified("set");
	
	protected Property    property;

	private   ModifierSet modifiers;
	private boolean targetSetter;

	public PropertyParser(Property property)
	{
		this.property = property;
		this.mode = GET_OR_SET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser();
			return;
		}
		
		switch (this.mode)
		{
		case GET_OR_SET:
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
				int nextType = token.next().type();
				if (nextType == BaseSymbols.COLON || nextType == BaseSymbols.CLOSE_CURLY_BRACKET
						|| nextType == BaseSymbols.SEMICOLON || nextType == BaseSymbols.OPEN_PARENTHESIS)
				{
					Name name = token.nameValue();
					if (name == get)
					{
						this.property.setGetterModifiers(this.modifiers);
						this.modifiers = null;
						this.mode = COLON;
						this.targetSetter = false;
						return;
					}
					if (name == set)
					{
						this.property.setSetterModifiers(this.modifiers);
						this.modifiers = null;
						this.mode = SET;
						this.targetSetter = true;
						return;
					}
				}
			}
			
			// No 'get:' or 'set:' tag -> Read-Only Property
			this.property.setGetterModifiers(this.modifiers);
			this.modifiers = null;
			this.mode = COLON;
			pm.pushParser(pm.newExpressionParser(this), true);
			return;
		case SET:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = SETTER_PARAMETER_NAME;
				return;
			}
			// Fallthrough
		case COLON:
			if (type == BaseSymbols.COLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == BaseSymbols.SEMICOLON)
			{
				this.mode = GET_OR_SET;
				return;
			}
			pm.report(token, "property.colon");
			return;
		case SETTER_PARAMETER_NAME:
			this.mode = SETTER_PARAMETER_NAME_END;
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "property.setter.identifier");
			}
			else
			{
				this.property.setSetterParameterName(token.nameValue());
			}
			return;
		case SETTER_PARAMETER_NAME_END:
			this.mode = COLON;
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
			this.property.setSetter(value);
		}
		else
		{
			this.property.setGetter(value);
		}
	}
}
