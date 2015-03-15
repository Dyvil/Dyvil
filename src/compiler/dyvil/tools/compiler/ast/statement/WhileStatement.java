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

public class WhileStatement extends ASTNode implements IStatement, ILoop
{
	public IValue		condition;
	public IValue		then;
	
	private IStatement	parent;
	
	public Label		startLabel;
	public Label		endLabel;
	
	public WhileStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label("$whileStart");
		this.endLabel = new Label("$whileEnd");
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
		return WHILE;
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
		return this.startLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
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
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			if (!this.condition.isType(Type.BOOLEAN))
			{
				Marker marker = markers.create(this.condition.getPosition(), "while.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
				
			}
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(this.position, "while.condition.invalid");
		}
		if (this.then != null)
		{
			this.then.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.then != null)
		{
			this.then = this.then.foldConstants();
		}
		return this;
	}
	
	@Override
	public Label resolveLabel(String name)
	{
		if ("$whileStart".equals(name))
		{
			return this.startLabel;
		}
		else if ("$whileEnd".equals(name))
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
		
		// Condition
		writer.writeFrameLabel(this.startLabel.target);
		this.condition.writeInvJump(writer, this.endLabel.target);
		// While Block
		this.then.writeStatement(writer);
		writer.writeJumpInsn(Opcodes.GOTO, this.startLabel.target);
		
		writer.writeFrameLabel(this.endLabel.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.whileStart);
		if (this.condition != null)
		{
			this.condition.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.whileEnd);
		
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
