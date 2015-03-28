package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class GoToStatement extends ASTNode implements IStatement
{
	public Label		label;
	public Name			name;
	
	private IStatement	parent;
	
	public GoToStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return GOTO;
	}
	
	public void setName(Name name)
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.parent == null)
		{
			markers.add(this.position, "goto.invalid");
			return this;
		}
		
		if (this.name != null)
		{
			this.label = this.parent.resolveLabel(this.name);
			
			if (this.label == null)
			{
				markers.add(this.position, "resolve.label", this.name);
			}
		}
		else
		{
			markers.add(this.position, "goto.invalid");
		}
		markers.add(this.position, "goto.warning");
		
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
		buffer.append("goto");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
