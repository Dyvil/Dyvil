package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Map;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;

public class ArrayLiteralParser extends Parser implements IValueConsumer
{
	protected static final int	OPEN_BRACKET	= 1;
	protected static final int	SEPARATOR		= 2;
	protected static final int	COLON			= 4;
	
	protected IValueConsumer consumer;
	
	private IToken startPosition;
	
	private IValue[]	values	= new IValue[3];
	private int			valueCount;
	private IValue[]	values2;
	private boolean		map;
	
	public ArrayLiteralParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = OPEN_BRACKET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case OPEN_BRACKET:
			pm.pushParser(new ExpressionParser(this));
			this.mode = SEPARATOR | COLON;
			this.startPosition = token;
			
			if (type != Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Array Literal - '[' expected"));
			}
			return;
		case SEPARATOR | COLON:
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}
			
			if (type == Symbols.COLON)
			{
				this.mode = SEPARATOR;
				this.map = true;
				this.values2 = new IValue[this.valueCount];
				pm.pushParser(new ExpressionParser(this));
				return;
			}
			
			this.mode = SEPARATOR;
			pm.pushParser(new ExpressionParser(this));
			if (type != Symbols.COMMA && type != Symbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Array Literal - ',' expected"));
			}
			return;
		case SEPARATOR:
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}
			
			this.mode = this.map ? COLON : SEPARATOR;
			pm.pushParser(new ExpressionParser(this));
			if (type != Symbols.COMMA && type != Symbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Array Literal - ',' expected"));
			}
			return;
		case COLON:
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				this.end(token);
				pm.popParser();
				return;
			}
			
			this.mode = SEPARATOR;
			pm.pushParser(new ExpressionParser(this));
			if (type != Symbols.COLON)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Map Literal - ':' expected"));
			}
			return;
		}
	}
	
	private void end(IToken token)
	{
		if (this.map)
		{
			Map map = new Map(this.startPosition.to(token), this.values, this.values2, this.valueCount);
			this.consumer.setValue(map);
			return;
		}
		
		Array array = new Array(this.startPosition.to(token), this.values, this.valueCount);
		this.consumer.setValue(array);
	}
	
	private void ensureCapacity(int cap)
	{
		if (cap > this.values.length)
		{
			IValue[] newValues = new IValue[cap];
			System.arraycopy(this.values, 0, newValues, 0, this.valueCount);
			this.values = newValues;
		}
		
		if (this.map && cap > this.values2.length)
		{
			IValue[] newValues = new IValue[cap];
			System.arraycopy(this.values2, 0, newValues, 0, this.valueCount);
			this.values2 = newValues;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.ensureCapacity(this.valueCount + 1);
		switch (this.mode)
		{
		case COLON:
		case SEPARATOR | COLON:
			this.values[this.valueCount] = value;
			this.valueCount++;
			return;
		case SEPARATOR:
			if (this.map)
			{
				this.values2[this.valueCount - 1] = value;
			}
			else
			{
				this.values[this.valueCount] = value;
				this.valueCount++;
			}
		}
	}
}
