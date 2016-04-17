package dyvil.tools.compiler.ast.statement.control;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class GoToStatement extends AbstractValue implements IStatement
{
	public Label label;
	public Name  name;
	
	public GoToStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return GOTO;
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
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.name == null)
		{
			return;
		}
		
		this.label = context.resolveLabel(this.name);
		if (this.label == null)
		{
			markers.add(Markers.semantic(this.position, "resolve.label", this.name));
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		markers.add(Markers.semantic(this.position, "goto.warning"));
		if (this.label == null)
		{
			markers.add(Markers.semantic(this.position, "goto.invalid"));
			return this;
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
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.visitJumpInsn(Opcodes.GOTO, this.label.getTarget());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("goto");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
