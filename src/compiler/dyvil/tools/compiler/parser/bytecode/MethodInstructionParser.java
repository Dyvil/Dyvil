package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.ast.bytecode.MethodInstruction;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class MethodInstructionParser extends Parser implements IInternalTyped
{
	private static final int	OWNER			= 1;
	private static final int	DOT				= 2;
	private static final int	PARAMETERS		= 4;
	private static final int	PARAMETERS_END	= 8;
	private static final int	COLON			= 16;
	
	protected MethodInstruction methodInstruction;
	
	public MethodInstructionParser(MethodInstruction fi)
	{
		this.methodInstruction = fi;
		this.mode = OWNER;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON)
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case OWNER:
			if (type == Keywords.INTERFACE)
			{
				this.methodInstruction.setInterface(true);
			}
			pm.pushParser(new InternalTypeParser(this), true);
			this.mode = DOT;
			return;
		case DOT:
			if (type != Symbols.DOT)
			{
				pm.report(token, "Invalid Method Instruction - '.' expected");
				return;
			}
			this.mode = PARAMETERS;
			IToken next = token.next();
			if (!ParserUtil.isIdentifier(next.type()))
			{
				pm.report(next, "Invalid Method Instruction - Identifier expected");
				return;
			}
			pm.skip();
			this.methodInstruction.setMethodName(next.nameValue().qualified);
			return;
		case PARAMETERS:
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new InternalTypeParser(this));
				this.mode = PARAMETERS_END;
				return;
			}
			pm.report(token, "Invalid Method Instruction - '(' expected");
			return;
		case PARAMETERS_END:
			if (type == Symbols.COMMA)
			{
				pm.pushParser(new InternalTypeParser(this));
				return;
			}
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				this.mode = COLON;
				return;
			}
			pm.report(token, "Invalid Method Instruction - ',' or ')' expected");
			break;
		case COLON:
			if (type != Symbols.COLON)
			{
				pm.report(token, "Invalid Method Instruction - ':' expected");
				return;
			}
			pm.pushParser(new InternalTypeParser(this));
			return;
		}
	}
	
	@Override
	public void setInternalType(String desc)
	{
		if (this.mode == DOT)
		{
			this.methodInstruction.setOwner(desc);
			return;
		}
		if (this.mode == COLON)
		{
			this.methodInstruction.setReturnDesc(desc);
			return;
		}
		
		this.methodInstruction.addArgument(desc);
	}
	
	@Override
	public String getInternalType()
	{
		return null;
	}
}
