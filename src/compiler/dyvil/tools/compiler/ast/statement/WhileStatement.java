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

public final class WhileStatement extends ASTNode implements IStatement, ILoop
{
	public static final Name	$whileStart	= Name.getQualified("$whileStart");
	public static final Name	$whileEnd	= Name.getQualified("$whileEnd");
	
	public IValue				condition;
	public IValue				action;
	
	private IStatement			parent;
	
	public Label				startLabel;
	public Label				endLabel;
	
	public WhileStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label($whileStart);
		this.endLabel = new Label($whileEnd);
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
		return WHILE;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Types.VOID || type == Types.UNKNOWN ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID || type == Types.UNKNOWN;
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
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN);
			if (condition1 == null)
			{
				Marker marker = markers.create(this.condition.getPosition(), "while.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
			}
			else
			{
				this.condition = condition1;
			}
			this.condition.checkTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(this.position, "while.condition.invalid");
		}
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		return this;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (name == $whileStart)
		{
			return this.startLabel;
		}
		if (name == $whileEnd)
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
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		// Condition
		writer.writeLabel(startLabel);
		this.condition.writeInvJump(writer, endLabel);
		// While Block
		this.action.writeStatement(writer);
		writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		
		writer.writeLabel(endLabel);
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
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
