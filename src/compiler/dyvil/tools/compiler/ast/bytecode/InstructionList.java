package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class InstructionList
{
	private IInstruction[] instructions = new IInstruction[3];
	private int     instructionCount;
	private Label[] labels;
	
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
	
	public void addInstruction(IInstruction insn, Label label)
	{
		int index = this.instructionCount++;
		if (index >= this.instructions.length)
		{
			IInstruction[] temp = new IInstruction[this.instructionCount];
			System.arraycopy(this.instructions, 0, temp, 0, this.instructions.length);
			this.instructions = temp;
		}
		this.instructions[index] = insn;
		
		if (this.labels == null)
		{
			this.labels = new Label[index + 1];
			this.labels[index] = label;
			return;
		}
		if (index >= this.labels.length)
		{
			Label[] temp = new Label[index + 1];
			System.arraycopy(this.labels, 0, temp, 0, this.labels.length);
			this.labels = temp;
		}
		this.labels[index] = label;
	}
	
	public void addLabel(Label label)
	{
		int index = this.instructionCount;
		if (this.labels == null)
		{
			this.labels = new Label[index + 1];
			this.labels[index] = label;
			return;
		}
		if (index >= this.labels.length)
		{
			Label[] temp = new Label[index + 1];
			System.arraycopy(this.labels, 0, temp, 0, this.labels.length);
			this.labels = temp;
		}
		this.labels[index] = label;
	}
	
	public Label resolveLabel(Name name)
	{
		if (this.labels == null)
		{
			return null;
		}
		
		for (Label label : this.labels)
		{
			if (label == null)
			{
				continue;
			}
			
			if (name == label.name)
			{
				return label;
			}
		}
		
		return null;
	}

	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.instructionCount; i++)
		{
			this.instructions[i].resolve(markers, this);
		}
	}

	public void write(MethodWriter writer) throws BytecodeException
	{
		if (this.labels == null)
		{
			for (int i = 0; i < this.instructionCount; i++)
			{
				this.instructions[i].write(writer);
			}
			return;
		}

		for (Label label : this.labels)
		{
			if (label != null)
			{
				label.target = new dyvil.tools.asm.Label();
			}
		}

		for (int i = 0; i < this.instructionCount; i++)
		{
			if (i < this.labels.length)
			{
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeLabel(l.target);
				}
			}

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

		boolean hasLabel = false;
		Label label = null;
		boolean labelNewline = Formatting.getBoolean("label.separator.newline_after");

		for (int i = 0; i < this.instructionCount; i++)
		{
			buffer.append(instructionPrefix);
			if (this.labels != null && i < this.labels.length && (label = this.labels[i]) != null)
			{
				buffer.append(label.name);

				if (Formatting.getBoolean("label.separator.space_before"))
				{
					buffer.append(' ');
				}
				buffer.append(':');

				if (labelNewline)
				{
					buffer.append('\n');
				}
				else if (Formatting.getBoolean("label.separator.space_after"))
				{
					buffer.append(' ');
				}

				hasLabel = true;
			}

			if (hasLabel)
			{
				if (labelNewline)
				{
					buffer.append(labelPrefix);
				}
				this.instructions[i].toString(labelPrefix, buffer);
			}
			else
			{
				buffer.append(instructionPrefix);
				this.instructions[i].toString(instructionPrefix, buffer);
			}
			buffer.append('\n');
		}

		buffer.append(prefix).append('}');
	}
}
