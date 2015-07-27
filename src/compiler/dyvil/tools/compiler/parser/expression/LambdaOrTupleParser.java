package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpression;
import dyvil.tools.compiler.ast.expression.Tuple;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class LambdaOrTupleParser extends EmulatorParser implements IParameterList
{
	protected static final int	START			= 1;
	protected static final int	PARAMETERS		= 2;
	protected static final int	PARAMETER_NAME	= 3;
	protected static final int	SEPARATOR		= 4;
	protected static final int	TUPLE			= 5;
	protected static final int	TUPLE_END		= 6;
	protected static final int	ARROW			= 7;
	protected static final int	END				= 8;
	
	protected IValueConsumer consumer;
	
	private IParameter[]	params;
	private int				parameterCount;
	
	private IValue value;
	
	public LambdaOrTupleParser(IValueConsumer consumer)
	{
		this.mode = START;
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
		switch (this.mode)
		{
		case START:
			this.firstToken = token;
			this.pm = pm;
			this.mode = PARAMETERS;
			
			IToken next = token.next();
			int nextNextType = next.next().type();
			// Special cases: (x) => ... or (x, y, ...) => ...
			if (nextNextType == Symbols.CLOSE_PARENTHESIS || nextNextType == Symbols.COMMA)
			{
				this.mode = PARAMETER_NAME;
				return;
			}
			
			this.parser = this.tryParser = new ParameterListParser(this);
			return;
		case PARAMETERS:
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
		case PARAMETER_NAME:
			if (!ParserUtil.isIdentifier(token.type()))
			{
				pm.jump(this.firstToken);
				this.mode = TUPLE;
				return;
			}
			this.mode = SEPARATOR;
			this.addParameter(new MethodParameter(token.raw(), token.nameValue()));
			return;
		case SEPARATOR:
			int type = token.type();
			if (type == Symbols.COMMA)
			{
				this.mode = PARAMETER_NAME;
				return;
			}
			if (type == Symbols.CLOSE_PARENTHESIS && token.next().type() == Symbols.ARROW_OPERATOR)
			{
				this.mode = ARROW;
				return;
			}
			
			pm.jump(this.firstToken);
			this.mode = TUPLE;
			return;
		case TUPLE:
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
		case TUPLE_END:
			pm.popParser();
			if (token.type() == Symbols.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				this.consumer.setValue(this.value);
				return;
			}
			throw new SyntaxError(token, "Invalid Tuple - ')' expected", true);
		case ARROW:
			if (token.type() != Symbols.ARROW_OPERATOR)
			{
				pm.jump(this.firstToken);
				this.mode = TUPLE;
				return;
			}
			
			LambdaExpression le = new LambdaExpression(token.raw(), this.params, this.parameterCount);
			pm.pushParser(pm.newExpressionParser(le));
			this.value = le;
			this.mode = END;
			return;
		case END:
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
			System.arraycopy(this.params, 0, temp, 0, this.params.length);
			this.params = temp;
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
