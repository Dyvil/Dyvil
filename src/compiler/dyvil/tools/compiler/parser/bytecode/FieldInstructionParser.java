package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.FieldInstruction;
import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class FieldInstructionParser extends Parser implements IInternalTyped
{
	private static final int OWNER = 1;
	private static final int DOT   = 2;
	private static final int COLON = 8;
	
	protected FieldInstruction fieldInstruction;
	
	public FieldInstructionParser(FieldInstruction fi)
	{
		this.fieldInstruction = fi;
		this.mode = OWNER;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.SEMICOLON)
		{
			pm.popParser(true);
			return;
		}
		
		switch (this.mode)
		{
		case OWNER:
			pm.pushParser(new InternalTypeParser(this), true);
			this.mode = DOT;
			return;
		case DOT:
			if (type != BaseSymbols.DOT)
			{
				pm.report(token, "Invalid Field Instruction - '.' expected");
				return;
			}
			this.mode = COLON;
			IToken next = token.next();
			if (!ParserUtil.isIdentifier(next.type()))
			{
				pm.report(next, "Invalid Field Instruction - Field Name expected");
				return;
			}
			pm.skip();
			this.fieldInstruction.setFieldName(next.nameValue().qualified);
			return;
		case COLON:
			if (type != BaseSymbols.COLON)
			{
				pm.report(token, "Invalid Field Instruction - ':' expected");
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
			this.fieldInstruction.setOwner(desc);
			return;
		}
		if (this.mode == COLON)
		{
			this.fieldInstruction.setDesc(desc);
		}
	}
	
	@Override
	public String getInternalType()
	{
		return null;
	}
}
