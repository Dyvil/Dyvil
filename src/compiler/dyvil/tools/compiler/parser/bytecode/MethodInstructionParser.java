package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.ast.bytecode.MethodInstruction;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class MethodInstructionParser extends Parser implements IInternalTyped
{
	private static final int	OWNER			= 1;
	private static final int	DOT				= 2;
	private static final int	PARAMETERS		= 4;
	private static final int	PARAMETERS_END	= 8;
	private static final int	COLON			= 16;
	
	protected MethodInstruction	methodInstruction;
	
	public MethodInstructionParser(MethodInstruction fi)
	{
		this.methodInstruction = fi;
		this.mode = OWNER;
	}
	
	@Override
	public void reset()
	{
		this.mode = OWNER;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == OWNER)
		{
			if (type == Keywords.INTERFACE)
			{
				this.methodInstruction.setInterface(true);
			}
			
			pm.pushParser(new InternalTypeParser(this), true);
			this.mode = DOT;
			return;
		}
		if (this.mode == DOT)
		{
			if (type != Tokens.DOT)
			{
				throw new SyntaxError(token, "Invalid Field Instruction - '.' expected");
			}
			this.mode = PARAMETERS;
			
			IToken next = token.next();
			if (!ParserUtil.isIdentifier(next.type()))
			{
				throw new SyntaxError(next, "Invalid Field Instruction - Field Name expected");
			}
			pm.skip();
			this.methodInstruction.setMethodName(next.nameValue().qualified);
			return;
		}
		if (this.mode == PARAMETERS)
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new InternalTypeParser(this));
				this.mode = PARAMETERS_END;
				return;
			}
			throw new SyntaxError(token, "Invalid Method Instruction - '(' expected");
		}
		if (this.mode == PARAMETERS_END)
		{
			if (type == Tokens.COMMA)
			{
				pm.pushParser(new InternalTypeParser(this));
				return;
			}
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				this.mode = COLON;
				return;
			}
			throw new SyntaxError(token, "Invalid Method Instruction - ',' or ')' expected");
		}
		if (this.mode == COLON)
		{
			if (type != Tokens.COLON)
			{
				throw new SyntaxError(token, "Invalid Field Instruction - ':' expected");
			}
			
			pm.pushParser(new InternalTypeParser(this));
			return;
		}
	}
	
	@Override
	public void setInternalType(String desc, Object type)
	{
		if (this.mode == DOT)
		{
			this.methodInstruction.setOwner(desc);
			return;
		}
		if (this.mode == COLON)
		{
			this.methodInstruction.setReturnDesc(desc);
			this.methodInstruction.setReturnType(type);
			return;
		}
		
		this.methodInstruction.addArgument(desc);
	}
	
	@Override
	public Object getInternalType()
	{
		return null;
	}
	
}
