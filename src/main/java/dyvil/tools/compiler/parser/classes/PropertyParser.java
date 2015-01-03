package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

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
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			if ("get".equals(value))
			{
				this.mode = GET;
				return true;
			}
			else if ("set".equals(value))
			{
				this.mode = SET;
				return true;
			}
			else if (";".equals(value))
			{
				return true;
			}
			else if ("}".equals(value))
			{
				pm.popParser(true);
				return true;
			}
		}
		if (this.isInMode(GET) || this.isInMode(SET))
		{
			if (":".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this));
				return true;
			}
			else if (";".equals(value))
			{
				this.mode = 0;
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
