package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.CatchBlock;
import dyvil.tools.compiler.ast.statement.TryStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class TryStatementParser extends Parser implements IValueConsumer
{
	private static final int	ACTION		= 1;
	private static final int	CATCH		= 2;
	private static final int	CATCH_OPEN	= 4;
	private static final int	CATCH_VAR	= 16;
	private static final int	CATCH_CLOSE	= 32;
	
	protected TryStatement	statement;
	private CatchBlock		catchBlock;
	
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
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = CATCH;
			return;
		}
		int type = token.type();
		if (this.mode == CATCH)
		{
			if (type == Keywords.CATCH)
			{
				this.statement.addCatchBlock(this.catchBlock = new CatchBlock(token.raw()));
				this.mode = CATCH_OPEN;
				return;
			}
			if (type == Keywords.FINALLY)
			{
				pm.popParser();
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = 0;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				IToken next = token.getNext();
				if (next == null)
				{
					pm.popParser(true);
					return;
				}
				
				int nextType = token.next().type();
				if (nextType != Keywords.CATCH && nextType != Keywords.FINALLY)
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
			pm.pushParser(pm.newTypeParser(this.catchBlock));
			if (type == Symbols.OPEN_PARENTHESIS)
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
				this.catchBlock.varName = token.nameValue();
				return;
			}
			throw new SyntaxError(token, "Invalid Catch Expression - Name expected", true);
		}
		if (this.mode == CATCH_CLOSE)
		{
			this.mode = CATCH;
			pm.pushParser(pm.newExpressionParser(this.catchBlock));
			if (type == Symbols.CLOSE_PARENTHESIS)
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
			this.statement.setAction(value);
			return;
		}
		if (this.mode == 0)
		{
			this.statement.setFinallyBlock(value);
		}
	}
}
