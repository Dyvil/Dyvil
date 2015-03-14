package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.statement.CatchBlock;
import dyvil.tools.compiler.ast.statement.TryStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class TryStatementParser extends Parser implements IValued
{
	private static final int	ACTION		= 1;
	private static final int	CATCH		= 2;
	private static final int	CATCH_OPEN	= 4;
	private static final int	CATCH_VAR	= 16;
	private static final int	CATCH_CLOSE	= 32;
	
	protected TryStatement		statement;
	private CatchBlock			catchBlock;
	
	public TryStatementParser(TryStatement statement)
	{
		this.statement = statement;
		this.mode = ACTION;
	}
	
	@Override
	public void reset()
	{
		this.mode = ACTION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == ACTION)
		{
			// TODO Try-With-Resource
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = CATCH;
			return;
		}
		int type = token.type();
		if (this.mode == CATCH)
		{
			if (type == Tokens.CATCH)
			{
				this.statement.addCatchBlock(this.catchBlock = new CatchBlock(token.raw()));
				this.mode = CATCH_OPEN;
				return;
			}
			if (type == Tokens.FINALLY)
			{
				pm.popParser();
				pm.pushParser(new ExpressionParser(this));
				this.mode = 0;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				if (!token.isInferred())
				{
					pm.popParser(true);
				}
				return;
			}
			pm.popParser(true);
		}
		if (this.mode == CATCH_OPEN)
		{
			this.mode = CATCH_VAR;
			pm.pushParser(new TypeParser(this.catchBlock));
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Catch Expression - '(' expected", true);
		}
		if (this.mode == CATCH_VAR)
		{
			this.mode = CATCH_CLOSE;
			if (ParserUtil.isIdentifier(type))
			{
				this.catchBlock.varName = token.value();
				return;
			}
			throw new SyntaxError(token, "Invalid Catch Expression - Name expected", true);
		}
		if (this.mode == CATCH_CLOSE)
		{
			this.mode = CATCH;
			pm.pushParser(new ExpressionParser(this.catchBlock));
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Catch Expression - ')' expected");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == CATCH)
		{
			this.statement.action = value;
			return;
		}
		if (this.mode == 0)
		{
			this.statement.finallyBlock = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
