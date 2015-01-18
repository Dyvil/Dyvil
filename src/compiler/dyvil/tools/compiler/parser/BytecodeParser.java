package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.bytecode.Instruction;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class BytecodeParser extends Parser
{
	public static final int	INSTRUCTION	= 1;
	public static final int	LABEL		= 2;
	public static final int	ARGUMENTS	= 4;
	
	public Bytecode			bytecode;
	
	private String			label;
	private Instruction		instruction;
	
	public BytecodeParser(Bytecode bytecode)
	{
		this.bytecode = bytecode;
		this.mode = INSTRUCTION | LABEL;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(LABEL))
		{
			if (token.next().isType(Tokens.SEMICOLON))
			{
				this.label = value;
				pm.skip();
				return true;
			}
		}
		int type = token.type();
		if (this.isInMode(INSTRUCTION))
		{
			if (ParserUtil.isIdentifier(type))
			{
				Instruction insn = Instruction.parse(value);
				if (insn == null)
				{
					this.mode = INSTRUCTION | LABEL;
					throw new SyntaxError(token, "Unknown Opcode '" + value + "'");
				}
				
				if (this.label != null)
				{
					this.bytecode.addInstruction(insn, this.label);
					this.label = null;
				}
				else
				{
					this.bytecode.addInstruction(insn);
				}
				
				insn.setPosition(token);
				this.instruction = insn;
				this.mode = ARGUMENTS;
				return true;
			}
		}
		if (this.isInMode(ARGUMENTS))
		{
			if (type == Tokens.SEMICOLON)
			{
				this.mode = INSTRUCTION | LABEL;
				return true;
			}
			if (type == Tokens.COMMA)
			{
				return true;
			}
			if (!ParserUtil.isTerminator(type))
			{
				if (this.instruction == null)
				{
					throw new SyntaxError(token, "Invalid Argument '" + value + "' for Unknown Opcode");
				}
				else if (!this.instruction.addArgument(token.object()))
				{
					throw new SyntaxError(token, "Invalid Argument '" + value + "' for Opcode " + this.instruction.getName());
				}
				return true;
			}
		}
		
		pm.popParser(true);
		return true;
	}
}
