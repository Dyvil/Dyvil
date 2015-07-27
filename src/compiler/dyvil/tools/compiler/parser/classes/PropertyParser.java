package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ModifierTypes;

public class PropertyParser extends Parser implements IValued
{
	private static final int	GET_OR_SET	= 1;
	private static final int	GET			= 2;
	private static final int	SET			= 4;
	
	public static final Name	get	= Name.getQualified("get");
	public static final Name	set	= Name.getQualified("set");
	
	protected Property	property;
	private int			modifiers;
	
	public PropertyParser(Property property)
	{
		this.property = property;
		this.mode = GET_OR_SET;
	}
	
	@Override
	public void reset()
	{
		this.mode = GET_OR_SET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser();
			return;
		}
		
		switch (this.mode)
		{
		case GET_OR_SET:
			if (type == Symbols.SEMICOLON)
			{
				return;
			}
			
			int mod;
			if ((mod = ModifierTypes.METHOD.parse(type)) >= 0)
			{
				this.modifiers |= mod;
				return;
			}
			
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				int nextType = token.next().type();
				if (nextType == Symbols.COLON || nextType == Symbols.CLOSE_CURLY_BRACKET || nextType == Symbols.SEMICOLON)
				{
					Name name = token.nameValue();
					if (name == get)
					{
						this.property.setGetterModifiers(this.modifiers);
						this.mode = GET;
						return;
					}
					if (name == set)
					{
						this.property.setSetterModifiers(this.modifiers);
						this.mode = SET;
						return;
					}
				}
			}
			
			// No 'get:' or 'set:' tag -> Read-Only Property
			this.property.setGetterModifiers(this.modifiers);
			this.mode = GET;
			pm.pushParser(pm.newExpressionParser(this), true);
			return;
		case GET:
		case SET:
			if (type == Symbols.COLON)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == Symbols.SEMICOLON || type == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.mode = GET_OR_SET;
				return;
			}
			throw new SyntaxError(token, "Invalid Property Declaration - ':' expected");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == GET)
		{
			this.property.setGetter(value);
		}
		else if (this.mode == SET)
		{
			this.property.setSetter(value);
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
