package dyvil.tools.compiler.ast.statement.control;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class BreakStatement extends AbstractValue implements IStatement
{
	public Label label;
	public Name  name;
	
	private IStatement parent;
	
	public BreakStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return BREAK;
	}
	
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.name == null)
		{
			ILoop loop = context.getEnclosingLoop();
			if (loop == null)
			{
				markers.add(I18n.createMarker(this.position, "break.invalid"));
				return;
			}
			
			this.label = loop.getBreakLabel();
			return;
		}
		
		this.label = context.resolveLabel(this.name);
		
		if (!(this.label.value instanceof ILoop))
		{
			markers.add(I18n.createMarker(this.position, "break.invalid.type", this.name));
			return;
		}
		
		this.label = ((ILoop) this.label.value).getBreakLabel();
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.label == null)
		{
			if (this.name == null)
			{
				markers.add(I18n.createMarker(this.position, "break.invalid"));
			}
			else
			{
				markers.add(I18n.createMarker(this.position, "resolve.label", this.name));
			}
		}
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
	public void writeStatement(MethodWriter writer) throws BytecodeException
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
