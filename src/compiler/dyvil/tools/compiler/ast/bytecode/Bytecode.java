package dyvil.tools.compiler.ast.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Bytecode extends ASTNode implements IValue
{
	private List<Instruction>	instructions	= new ArrayList();
	
	private Map<String, Label>	labels			= new HashMap();
	public Label				endLabel		= new Label();
	
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
	
	public void addInstruction(Instruction insn)
	{
		this.instructions.add(insn);
	}
	
	public void addInstruction(Instruction insn, String label)
	{
		this.instructions.add(insn);
		Label l = new Label();
		l.info = label;
		insn.label = l;
		this.labels.put(label, l);
	}
	
	public Label getLabel(String name)
	{
		return this.labels.get(name);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		for (Instruction i : this.instructions)
		{
			i.resolve(markers, this);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
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
		for (Instruction i : this.instructions)
		{
			if (i.label != null)
			{
				writer.writeFrameLabel(i.label);
			}
			i.write(writer);
		}
		writer.writeFrameLabel(this.endLabel);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		for (Instruction i : this.instructions)
		{
			if (i.label != null)
			{
				writer.writeFrameLabel(i.label);
			}
			i.write(writer);
		}
		writer.writeFrameLabel(this.endLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instructions.isEmpty())
		{
			buffer.append('@').append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append('\n').append(prefix).append("@{\n");
			for (Instruction i : this.instructions)
			{
				buffer.append(prefix).append(Formatting.Method.indent);
				if (i.label != null)
				{
					buffer.append(i.label.info).append(": ");
				}
				i.toString("", buffer);
				buffer.append(";\n");
			}
			buffer.append(prefix).append('}');
		}
	}
}
