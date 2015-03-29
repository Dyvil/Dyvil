package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.FieldInstruction;
import dyvil.tools.compiler.ast.bytecode.IInternalTyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class FieldInstructionParser extends Parser implements IInternalTyped
{
	private static final int	OWNER	= 1;
	private static final int	DOT		= 2;
	private static final int	COLON	= 8;
	
	protected FieldInstruction	fieldInstruction;
	
	public FieldInstructionParser(FieldInstruction fi)
	{
		this.fieldInstruction = fi;
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
			this.mode = COLON;
			
			IToken next = token.next();
			if (!ParserUtil.isIdentifier(next.type()))
			{
				throw new SyntaxError(next, "Invalid Field Instruction - Field Name expected");
			}
			pm.skip();
			this.fieldInstruction.setFieldName(next.nameValue().qualified);
			return;
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
			this.fieldInstruction.setOwner(desc);
			return;
		}
		if (this.mode == COLON)
		{
			this.fieldInstruction.setDesc(desc);
			this.fieldInstruction.setType(type);
		}
	}
	
	@Override
	public Object getInternalType()
	{
		return null;
	}
	
}
