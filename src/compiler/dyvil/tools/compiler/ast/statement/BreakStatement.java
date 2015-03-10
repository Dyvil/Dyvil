package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BreakStatement extends ASTNode implements IStatement
{
	public Label		label;
	public String		name;
	
	private IStatement	parent;
	
	public BreakStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return BREAK;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.parent == null)
		{
			markers.add(Markers.create(this.position, "break.invalid"));
			return this;
		}
		
		if (this.name != null)
		{
			this.label = this.parent.resolveLabel(this.name);
			
			if (this.label == null)
			{
				markers.add(Markers.create(this.position, "resolve.label", this.name));
			}
			else if (this.label.value instanceof ILoop)
			{
				this.label = ((ILoop) this.label.value).getBreakLabel();
			}
			else
			{
				markers.add(Markers.create(this.position, "break.invalid.label", this.name));
			}
		}
		else
		{
			IStatement parent = this.parent;
			while (parent != null)
			{
				if (parent instanceof ILoop)
				{
					this.label = ((ILoop) parent).getBreakLabel();
					break;
				}
				parent = parent.getParent();
			}
			
			if (this.label == null)
			{
				markers.add(Markers.create(this.position, "break.invalid"));
			}
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
		writer.writeJumpInsn(Opcodes.GOTO, this.label.target);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeJumpInsn(Opcodes.GOTO, this.label.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("break");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
