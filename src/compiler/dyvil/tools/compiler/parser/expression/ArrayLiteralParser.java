package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.MapExpr;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ArrayLiteralParser extends Parser implements IValueConsumer
{
	protected static final int OPEN_BRACKET = 1;
	protected static final int SEPARATOR    = 2;
	protected static final int COLON        = 4;
	
	protected IValueConsumer consumer;
	
	private IToken startPosition;
	
	private IValue[] keys = new IValue[3];
	private int      keyCount;
	private IValue[] values;
	private boolean  map;
	
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
			pm.pushParser(this.newExpressionParser(pm));
			this.mode = SEPARATOR | COLON;
			this.startPosition = token;
			
			if (type != BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "array.open_bracket");
			}
			return;
		case SEPARATOR | COLON:
			if (type == BaseSymbols.COLON)
			{
				this.mode = SEPARATOR;
				this.map = true;
				this.values = new IValue[this.keyCount];
				pm.pushParser(this.newExpressionParser(pm));
				return;
			}
			this.map = false;
			// Fallthrough
		case SEPARATOR:
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}
			
			this.mode = this.map ? COLON : SEPARATOR;
			pm.pushParser(this.newExpressionParser(pm));
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.report(token, "array.separator");
			}
			return;
		case COLON:
			if (type == BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.popParser();
				this.end(token);
				return;
			}
			
			this.mode = SEPARATOR;
			pm.pushParser(this.newExpressionParser(pm));
			if (type != BaseSymbols.COLON)
			{
				pm.reparse();
				pm.report(token, "array.map.colon");
			}
			return;
		}
	}

	private ExpressionParser newExpressionParser(IParserManager pm)
	{
		return new ExpressionParser(this).withFlag(ExpressionParser.IGNORE_COLON);
	}
	
	private void end(IToken token)
	{
		if (this.map)
		{
			MapExpr map = new MapExpr(this.startPosition.to(token), this.keys, this.values, this.keyCount);
			this.consumer.setValue(map);
			return;
		}
		
		ArrayExpr array = new ArrayExpr(this.startPosition.to(token), this.keys, this.keyCount);
		this.consumer.setValue(array);
	}
	
	private void ensureCapacity(int cap)
	{
		if (cap > this.keys.length)
		{
			IValue[] newValues = new IValue[cap];
			System.arraycopy(this.keys, 0, newValues, 0, this.keyCount);
			this.keys = newValues;
		}
		
		if (this.map && cap > this.values.length)
		{
			IValue[] newValues = new IValue[cap];
			System.arraycopy(this.values, 0, newValues, 0, this.keyCount);
			this.values = newValues;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.ensureCapacity(this.keyCount + 1);
		switch (this.mode)
		{
		case COLON:
		case SEPARATOR | COLON:
			this.keys[this.keyCount] = value;
			this.keyCount++;
			return;
		case SEPARATOR:
			if (this.map)
			{
				this.values[this.keyCount - 1] = value;
			}
			else
			{
				this.keys[this.keyCount] = value;
				this.keyCount++;
			}
		}
	}
}
