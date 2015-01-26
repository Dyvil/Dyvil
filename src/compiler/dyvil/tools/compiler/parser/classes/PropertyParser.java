package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.util.Tokens;

public class PropertyParser extends Parser implements IValued
{
	public static final int	GET	= 1;
	public static final int	SET	= 2;
	
	public IContext			context;
	public Property			property;
	
	public PropertyParser(IContext context, Property property)
	{
		this.context = context;
		this.property = property;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			return true;
		}
		if (type == Tokens.CLOSE_CURLY_BRACKET)
		{
			pm.popParser(true);
			return true;
		}
		
		if (this.mode == 0)
		{
			String value = token.value();
			if ("get".equals(value))
			{
				this.mode = GET;
				return true;
			}
			if ("set".equals(value))
			{
				this.mode = SET;
				return true;
			}
		}
		if (this.isInMode(GET) || this.isInMode(SET))
		{
			if (type == Tokens.COLON)
			{
				pm.pushParser(new ExpressionParser(this));
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == GET)
		{
			this.property.get = value;
		}
		else if (this.mode == SET)
		{
			this.property.set = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
