package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class DoStatement extends ASTNode implements IStatement, ILoop
{
	public static final Name	$doStart		= Name.getQualified("$doStart");
	public static final Name	$doCondition	= Name.getQualified("$doCondition");
	public static final Name	$doEnd			= Name.getQualified("$doEnd");
	
	public IValue				action;
	public IValue				condition;
	
	private IStatement			parent;
	
	public Label				startLabel;
	public Label				conditionLabel;
	public Label				endLabel;
	
	public DoStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label($doStart);
		this.conditionLabel = new Label($doCondition);
		this.endLabel = new Label($doEnd);
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
		this.action = then;
	}
	
	public IValue getThen()
	{
		return this.action;
	}
	
	@Override
	public int valueTag()
	{
		return DO_WHILE;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.classEquals(Types.VOID);
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
		if (this.action != null)
		{
			if (this.action.isStatement())
			{
				((IStatement) this.action).setParent(this);
				this.action.resolveTypes(markers, context);
			}
			else
			{
				this.action.resolveTypes(markers, context);
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
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN);
			if (condition1 == null)
			{
				Marker marker = markers.create(this.condition.getPosition(), "do.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
			}
			else
			{
				this.condition = condition1;
			}
			
			this.condition.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
		if (this.condition != null)
		{
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
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		return this;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (name == $doCondition)
		{
			return this.conditionLabel;
		}
		if (name == $doEnd)
		{
			return this.endLabel;
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.action == null)
		{
			this.condition.writeStatement(writer);
		}
		
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label conditionLabel = this.conditionLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		// Do Block
		
		writer.writeLabel(startLabel);
		this.action.writeStatement(writer);
		// Condition
		writer.writeLabel(conditionLabel);
		this.condition.writeJump(writer, startLabel);
		
		writer.writeLabel(endLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.doStart);
		if (this.action != null)
		{
			if (this.action.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.action.toString(prefix, buffer);
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
				this.action.toString(prefix, buffer);
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
