package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class Bytecode implements IValue
{
	protected ICodePosition position;
	
	private IInstruction[]	instructions	= new IInstruction[3];
	private int				instructionCount;
	private Label[]			labels;
	
	public Bytecode(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return BYTECODE;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
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
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.instructionCount; i++)
		{
			this.instructions[i].resolve(markers, this);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
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
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instructionCount == 0)
		{
			buffer.append('@').append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append("@ {\n");
			String prefix1 = prefix + Formatting.Method.indent;
			boolean label = false;
			for (int i = 0; i < this.instructionCount; i++)
			{
				buffer.append(prefix1);
				if (this.labels != null && i < this.labels.length)
				{
					Label l = this.labels[i];
					if (l != null)
					{
						buffer.append(l.name).append(Formatting.Expression.labelSeperator).append(prefix1);
						label = true;
					}
				}
				
				if (label)
				{
					buffer.append(' ');
				}
				this.instructions[i].toString(prefix1, buffer);
				buffer.append('\n');
			}
			buffer.append(prefix).append('}');
		}
	}
}
