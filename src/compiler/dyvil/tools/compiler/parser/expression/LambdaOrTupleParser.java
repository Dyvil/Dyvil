package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IParameterConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.expression.TupleExpr;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.EmulatorParser;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class LambdaOrTupleParser extends EmulatorParser implements IParameterConsumer
{
	protected static final int START          = 1;
	protected static final int PARAMETERS     = 2;
	protected static final int PARAMETER_NAME = 3;
	protected static final int SEPARATOR      = 4;
	protected static final int TUPLE          = 5;
	protected static final int TUPLE_END      = 6;
	protected static final int ARROW          = 7;
	protected static final int END            = 8;
	
	protected IValueConsumer consumer;
	
	private IParameter[] params;
	private int          parameterCount;
	
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
			if (token.type() == BaseSymbols.CLOSE_PARENTHESIS && this.tryParser.isInMode(ParameterListParser.SEPARATOR))
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
			final IParameter methodParameter = this.createParameter(token.raw(), token.nameValue(), Types.UNKNOWN,
			                                                            EmptyModifiers.INSTANCE, null);
			this.addParameter(methodParameter);
			return;
		case SEPARATOR:
			int type = token.type();
			if (type == BaseSymbols.COMMA)
			{
				this.mode = PARAMETER_NAME;
				return;
			}
			if (type == BaseSymbols.CLOSE_PARENTHESIS && token.next().type() == DyvilSymbols.DOUBLE_ARROW_RIGHT)
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
				TupleExpr t = new TupleExpr(token);
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
			pm.report(token, "tuple.close_paren");
			return;
		case ARROW:
			if (token.type() != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.jump(this.firstToken);
				this.mode = TUPLE;
				return;
			}
			
			LambdaExpr le = new LambdaExpr(token.raw(), this.params, this.parameterCount);
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
	public void addParameter(IParameter parameter)
	{
		if (this.params == null)
		{
			this.params = new IParameter[3];
			this.params[0] = parameter;
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
		this.params[index] = parameter;
	}
}
