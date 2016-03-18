package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;

public class InstructionList
{
	protected IInstruction[] instructions = new IInstruction[3];
	protected int instructionCount;

	public InstructionList()
	{

	}
	
	public void addInstruction(IInstruction insn)
	{
		int index = this.instructionCount++;
		if (index >= this.instructions.length)
		{
			IInstruction[] temp = new IInstruction[this.instructionCount];
			System.arraycopy(this.instructions, 0, temp, 0, this.instructions.length);
			this.instructions = temp;
		}
		this.instructions[index] = insn;
	}

	public void write(MethodVisitor writer) throws BytecodeException
	{
		for (int i = 0; i < this.instructionCount; i++)
		{
			this.instructions[i].write(writer);
		}
	}

	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('@');

		if (Formatting.getBoolean("bytecode.start.space_after"))
		{
			buffer.append(' ');
		}

		if (this.instructionCount == 0)
		{
			if (Formatting.getBoolean("statement.empty.newline"))
			{
				buffer.append('{').append('\n').append(prefix).append('}');
			}
			else if (Formatting.getBoolean("statement.empty.space_between"))
			{
				buffer.append("{ }");
			}
			else
			{
				buffer.append("{}");
			}
			return;
		}

		if (Formatting.getBoolean("statementment.open_brace.newline"))
		{
			buffer.append('\n').append(prefix).append('{').append(prefix);
		}

		String instructionPrefix = Formatting.getIndent("statement.indent", prefix);
		String labelPrefix = Formatting.getIndent("label.indent", instructionPrefix);

		for (int i = 0; i < this.instructionCount; i++)
		{
			buffer.append(instructionPrefix);
			this.instructions[i].toString(instructionPrefix, buffer);

			buffer.append('\n');
		}

		buffer.append(prefix).append('}');
	}
}
