package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpression;
import dyvil.tools.compiler.ast.expression.Tuple;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

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
	public void report(IToken token, String message)
	{
		this.pm.jump(this.firstToken);
		this.pm.setParser(this);
		this.mode = TUPLE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
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
			if (nextNextType == BaseSymbols.CLOSE_PARENTHESIS || nextNextType == BaseSymbols.COMMA)
			{
				this.mode = PARAMETER_NAME;
				return;
			}
			
			this.parser = this.tryParser = new ParameterListParser(this);
			return;
		case PARAMETERS:
			if (token.type() == BaseSymbols.CLOSE_PARENTHESIS && this.tryParser.isInMode(ParameterListParser.SEPERATOR))
			{
				this.mode = ARROW;
				return;
			}
			
			this.pm = pm;
			this.parser.parse(this, token);
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
			if (type == BaseSymbols.COMMA)
			{
				this.mode = PARAMETER_NAME;
				return;
			}
			if (type == BaseSymbols.CLOSE_PARENTHESIS && token.next().type() == DyvilSymbols.ARROW_OPERATOR)
			{
				this.mode = ARROW;
				return;
			}
			
			pm.jump(this.firstToken);
			this.mode = TUPLE;
			return;
		case TUPLE:
			if (token.type() == BaseSymbols.OPEN_PARENTHESIS)
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
			if (token.type() == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.value.expandPosition(token);
				this.consumer.setValue(this.value);
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Tuple - ')' expected");
			return;
		case ARROW:
			if (token.type() != DyvilSymbols.ARROW_OPERATOR)
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
