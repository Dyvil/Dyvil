package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.bytecode.Bytecode;
import dyvil.tools.compiler.ast.bytecode.Instruction;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.util.ClassFormat;

public class BytecodeParser extends Parser
{
	public static final int	INSTRUCTION	= 1;
	public static final int	LABEL		= 2;
	public static final int	ARGUMENTS	= 4;
	
	public IContext			context;
	public Bytecode			bytecode;
	
	private String			label;
	private Instruction		instruction;
	
	public BytecodeParser(IContext context, Bytecode bytecode)
	{
		this.context = context;
		this.bytecode = bytecode;
		this.mode = INSTRUCTION | LABEL;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(LABEL))
		{
			if (token.next().equals(":"))
			{
				this.label = value;
				pm.skip();
				return true;
			}
		}
		if (this.isInMode(INSTRUCTION))
		{
			if (token.isType(Token.TYPE_IDENTIFIER))
			{
				int opcode = ClassFormat.parseOpcode(value);
				Instruction insn = Instruction.fromOpcode(opcode, value);
				if (insn == null)
				{
					this.mode = INSTRUCTION | ARGUMENTS;
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
			if (";".equals(value))
			{
				this.mode = INSTRUCTION | LABEL;
				return true;
			}
			else if (",".equals(value))
			{
				return true;
			}
			else if (!token.isType(Token.TYPE_SYMBOL) && !token.isType(Token.TYPE_BRACKET))
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
