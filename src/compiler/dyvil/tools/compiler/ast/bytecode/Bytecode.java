package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class Bytecode extends ASTNode implements IValue
{
	private IInstruction[]	instructions	= new IInstruction[3];
	private int				instructionCount;
	private Label[]			labels;
	
	public Bytecode(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return BYTECODE;
	}
	
	@Override
	public Type getType()
	{
		return Type.VOID;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Type.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.VOID;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
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
	
	public Label resolveLabel(String name)
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
			
			if (name.equals(label.name))
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
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		for (int i = 0; i < this.instructionCount; i++)
		{
			if (this.labels != null && i < this.labels.length)
			{
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeFrameLabel(l.target);
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
