package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpression;
import dyvil.tools.compiler.ast.expression.Tuple;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.Symbols;

public class LambdaOrTupleParser extends EmulatorParser implements IParameterList
{
	protected static final int	START		= 0;
	protected static final int	PARAMETERS	= 1;
	protected static final int	TUPLE		= 2;
	protected static final int	TUPLE_END	= 4;
	protected static final int	ARROW		= 8;
	protected static final int	END			= 16;
	
	protected IValueConsumer	consumer;
	
	private IParameter[]		params;
	private int					parameterCount;
	
	private IValue				value;
	
	public LambdaOrTupleParser(IValueConsumer consumer)
	{
		this.mode = START;
		this.parser = this.tryParser = new ParameterListParser(this);
		this.consumer = consumer;
	}
	
	@Override
	public void reset()
	{
		this.mode = START;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == START)
		{
			this.firstToken = token;
			this.pm = pm;
			this.mode = PARAMETERS;
			return;
		}
		if (this.mode == PARAMETERS)
		{
			if (token.type() == Symbols.CLOSE_PARENTHESIS && this.tryParser.isInMode(ParameterListParser.SEPERATOR))
			{
				this.mode = ARROW;
				return;
			}
			
			try
			{
				this.pm = pm;
				this.parser.parse(this, token);
			}
			catch (SyntaxError error)
			{
				pm.jump(this.firstToken);
				pm.setParser(this);
				this.mode = TUPLE;
			}
			return;
		}
		if (this.mode == TUPLE)
		{
			if (token.type() == Symbols.OPEN_PARENTHESIS)
			{
				Tuple t = new Tuple(token);
				this.value = t;
				this.mode = TUPLE_END;
				pm.pushParser(new ExpressionListParser(t));
				return;
			}
			pm.popParser();
			return;
		}
		if (this.mode == TUPLE_END)
		{
			pm.popParser();
			if (token.type() == Symbols.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				this.consumer.setValue(this.value);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple - ')' expected", true);
		}
		if (this.mode == ARROW)
		{
			if (token.type() != Symbols.ARROW_OPERATOR)
			{
				pm.jump(this.firstToken);
				this.mode = TUPLE;
				return;
			}
			
			LambdaExpression le = new LambdaExpression(token.raw(), params, this.parameterCount);
			pm.pushParser(new ExpressionParser(le));
			this.value = le;
			this.mode = END;
			return;
		}
		if (this.mode == END)
		{
			pm.popParser(true);
			this.consumer.setValue(this.value);
			return;
		}
	}
	
	@Override
	public void addParameter(IParameter param)
	{
		if (this.params == null)
		{
			this.params = new IParameter[3];
			this.params[0] = param;
			this.parameterCount = 1;
			return;
		}
		
		int index = this.parameterCount++;
		if (index >= this.params.length)
		{
			IParameter[] temp = new IParameter[index + 1];
			System.arraycopy(params, 0, temp, 0, params.length);
			params = temp;
		}
		this.params[index] = param;
	}
	
	@Override
	public int parameterCount()
	{
		return 0;
	}
	
	@Override
	public void setParameter(int index, IParameter param)
	{
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		return null;
	}
	
	@Override
	public IParameter[] getParameters()
	{
		return null;
	}
}
