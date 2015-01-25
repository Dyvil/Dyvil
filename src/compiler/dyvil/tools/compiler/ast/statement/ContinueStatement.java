package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ContinueStatement extends ASTNode implements IStatement
{
	public Label		label;
	public String		name;
	
	private IStatement	parent;
	
	public ContinueStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return CONTINUE;
	}
	
	@Override
	public IType getType()
	{
		return null;
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
			markers.add(Markers.create(this.position, "continue.invalid"));
			return this;
		}
		
		if (this.name != null)
		{
			this.label = this.parent.resolveLabel(this.name);
			
			if (this.label == null)
			{
				markers.add(Markers.create(this.position, "resolve.label", this.name));
			}
			else if (this.label.info instanceof IValue)
			{
				IValue value = (IValue) this.label.info;
				if (value instanceof ILoop)
				{
					this.label = ((ILoop) value).getContinueLabel();
				}
				else
				{
					markers.add(Markers.create(this.position, "continue.invalid.label", this.name));
				}
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
				markers.add(Markers.create(this.position, "continue.invalid"));
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
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitJumpInsn(Opcodes.GOTO, this.label);
	}
	
	@Override
	public void setType(IType type)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("continue");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
