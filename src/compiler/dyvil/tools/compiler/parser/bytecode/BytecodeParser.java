package dyvil.tools.compiler.parser.bytecode;

import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.bytecode.Instruction;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
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
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		if (this.isInMode(LABEL))
		{
			if (token.next().type() == Tokens.COLON)
			{
				this.label = token.value();
				pm.skip();
				return;
			}
		}
		int type = token.type();
		if (this.isInMode(INSTRUCTION))
		{
			if (ParserUtil.isIdentifier(type))
			{
				String name = token.value();
				Instruction insn = Instruction.parse(name);
				if (insn == null)
				{
					this.mode = INSTRUCTION | LABEL;
					throw new SyntaxError(token, "Unknown Opcode '" + name + "'");
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
				return;
			}
		}
		if (this.isInMode(ARGUMENTS))
		{
			if (type == Tokens.SEMICOLON)
			{
				this.mode = INSTRUCTION | LABEL;
				return;
			}
			if (type == Tokens.COMMA)
			{
				return;
			}
			if (!ParserUtil.isTerminator(type))
			{
				if (this.instruction == null)
				{
					throw new SyntaxError(token, "Invalid Argument '" + token.value() + "' for Unknown Opcode");
				}
				else if (!this.instruction.addArgument(token.object()))
				{
					throw new SyntaxError(token, "Invalid Argument '" + token.value() + "' for Opcode " + this.instruction.getName());
				}
				return;
			}
		}
		
		pm.popParser(true);
		return;
	}
}
