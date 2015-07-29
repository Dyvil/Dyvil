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
	public void parse(IParserManager pm, IToken token) 
	{
		int type = token.type();
		switch (this.mode)
		{
		case ACTION:
			// TODO Try-With-Resource
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = CATCH;
			return;			
		case CATCH:
			if (type == Keywords.CATCH)
			{
				this.statement.addCatchBlock(this.catchBlock = new CatchBlock(token.raw()));
				this.mode = CATCH_OPEN;
				return;
			}
			if (type == Keywords.FINALLY)
			{
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				IToken next = token.next();
				if (next == null)
				{
					pm.popParser(true);
					return;
				}
				
				int nextType = token.next().type();
				if (nextType == Keywords.CATCH || nextType == Keywords.FINALLY)
				{
					return;
				}
			}
			pm.popParser(true);
			return;
		case CATCH_OPEN:
			this.mode = CATCH_VAR;
			pm.pushParser(pm.newTypeParser(this.catchBlock));
			if (type != Symbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Catch Expression - '(' expected"));
			}
			return;
		case CATCH_VAR:
			this.mode = CATCH_CLOSE;
			if (ParserUtil.isIdentifier(type))
			{
				this.catchBlock.varName = token.nameValue();
				return;
			}
			pm.reparse();
			pm.report(new SyntaxError(token, "Invalid Catch Expression - Name expected"));
			return;
		case CATCH_CLOSE:
			this.mode = CATCH;
			pm.pushParser(pm.newExpressionParser(this.catchBlock));
			if (type != Symbols.CLOSE_PARENTHESIS)
			{
				pm.report(new SyntaxError(token, "Invalid Catch Expression - ')' expected"));
			}
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case CATCH:
			this.statement.setAction(value);
			return;
		case END:
			this.statement.setFinallyBlock(value);
			break;
		}
	}
}
