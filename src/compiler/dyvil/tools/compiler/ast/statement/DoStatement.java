package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
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
	
	public IValue	action;
	public IValue	condition;
	
	public Label	startLabel;
	public Label	conditionLabel;
	public Label	endLabel;
	
	public DoStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label($doStart);
		this.conditionLabel = new Label($doCondition);
		this.endLabel = new Label($doEnd);
	}
	
	@Override
	public int valueTag()
	{
		return DO_WHILE;
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
		
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
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
			this.action.checkTypes(markers, context);
		}
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN, null, markers, context);
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
			if (this.condition.valueTag() == BOOLEAN && !((BooleanValue) this.condition).value)
			{
				return new VoidValue(this.position);
			}
			this.condition = this.condition.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.action != null)
		{
			this.action = this.action.cleanup(context, compilableList);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context, compilableList);
		}
		return this;
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
		
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label conditionLabel = this.conditionLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		// Do Block
		
		writer.writeTargetLabel(startLabel);
		this.action.writeStatement(writer);
		// Condition
		writer.writeLabel(conditionLabel);
		if (this.condition != null)
		{
			this.condition.writeJump(writer, startLabel);
		}
		else
		{
			writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		}
		
		writer.writeLabel(endLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.doStart);
		if (this.action != null)
		{
			buffer.append(' ');
			this.action.toString(prefix, buffer);
		}
		
		if (this.condition != null)
		{
			buffer.append(' ');
			buffer.append(Formatting.Statements.doWhile);
			this.condition.toString(prefix, buffer);
			buffer.append(Formatting.Statements.doEnd);
		}
	}
}
