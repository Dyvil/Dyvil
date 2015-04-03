package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class PropertyParser extends Parser implements IValued
{
	public static final int		GET	= 1;
	public static final int		SET	= 2;
	
	public static final Name	get	= Name.getQualified("get");
	public static final Name	set	= Name.getQualified("set");
	
	protected IContext			context;
	protected Property			property;
	
	public PropertyParser(IContext context, Property property)
	{
		this.context = context;
		this.property = property;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON)
		{
			this.mode = 0;
			return;
		}
		if (type == Symbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser();
			return;
		}
		
		if (this.mode == 0)
		{
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == get)
				{
					this.mode = GET;
					return;
				}
				if (name == set)
				{
					this.mode = SET;
					return;
				}
			}
			throw new SyntaxError(token, "Invalid Property Declaration - 'get' or 'set' expected", false);
		}
		if (this.mode > 0) // SET or GET
		{
			if (type == Symbols.COLON)
			{
				pm.pushParser(new ExpressionParser(this));
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
