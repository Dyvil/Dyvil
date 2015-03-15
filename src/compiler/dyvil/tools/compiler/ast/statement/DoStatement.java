package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class DoStatement extends ASTNode implements IStatement, ILoop
{
	public IValue		then;
	public IValue		condition;
	
	private IStatement	parent;
	
	public Label		startLabel;
	public Label		conditionLabel;
	public Label		endLabel;
	
	public DoStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label("$doStart");
		this.conditionLabel = new Label("$doCondition");
		this.endLabel = new Label("$doEnd");
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public int getValueType()
	{
		return DO_WHILE;
	}
	
	@Override
	public IType getType()
	{
		return Type.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.classEquals(Type.VOID);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
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
	public Label getContinueLabel()
	{
		return this.conditionLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				((IStatement) this.then).setParent(this);
				this.then.resolveTypes(markers, context);
			}
			else
			{
				this.then.resolveTypes(markers, context);
			}
		}
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, context);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.then != null)
		{
			this.then.check(markers, context);
		}
		if (this.condition != null)
		{
			if (!this.condition.isType(Type.BOOLEAN))
			{
				Marker marker = markers.create(this.condition.getPosition(), "do.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
				
			}
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(this.position, "do.condition.invalid");
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.then != null)
		{
			this.then = this.then.foldConstants();
		}
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		return this;
	}
	
	@Override
	public Label resolveLabel(String name)
	{
		if ("$doCondition".equals(name))
		{
			return this.conditionLabel;
		}
		else if ("$doEnd".equals(name))
		{
			return this.endLabel;
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.writeStatement(writer);
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeStatement(writer);
		}
		
		// Do Block
		writer.writeFrameLabel(this.startLabel.target);
		this.then.writeStatement(writer);
		// Condition
		writer.writeFrameLabel(this.conditionLabel.target);
		this.condition.writeJump(writer, this.startLabel.target);
		
		writer.writeFrameLabel(this.endLabel.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.doStart);
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.then.toString(prefix, buffer);
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
				this.then.toString(prefix, buffer);
				buffer.append(' ');
			}
		}
		
		buffer.append(Formatting.Statements.doWhile);
		if (this.condition != null)
		{
			this.condition.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.doEnd);
	}
}
