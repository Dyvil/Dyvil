package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ForStatement extends ASTNode implements IStatement, ILoop
{
	private IStatement	parent;
	
	public IType		variableType;
	public String		variableName;
	public IValue		variableValue;
	
	public IValue		condition;
	public IValue		update;
	
	public boolean		isForeach;
	
	public IValue		then;
	
	protected Label		start;
	protected Label		end;
	
	public ForStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return FOR;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public void setType(IType type)
	{
		this.variableType = type;
	}
	
	@Override
	public Label getStartLabel()
	{
		return this.start;
	}
	
	@Override
	public Label getEndLabel()
	{
		return this.end;
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
	public Label resolveLabel(String name)
	{
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		if (this.isForeach)
		{
			this.variableType.toString(prefix, buffer);
			buffer.append(' ').append(this.variableName).append(Formatting.Statements.forEachSeperator);
			this.variableValue.toString(prefix, buffer);
		}
		else
		{
			if (this.variableType != null)
			{
				this.variableType.toString(prefix, buffer);
				buffer.append(' ').append(this.variableName).append(Formatting.Field.keyValueSeperator).append(' ');
				this.variableValue.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.condition != null)
			{
				buffer.append(' ');
				this.condition.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.update != null)
			{
				buffer.append(' ');
				this.update.toString(prefix, buffer);
			}
		}
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.then.toString(prefix, buffer);
			}
			else
			{
				buffer.append(' ');
				this.then.toString(prefix, buffer);
			}
		}
	}
}
