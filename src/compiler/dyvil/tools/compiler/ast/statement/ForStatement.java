package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ForStatement implements IStatement, IDefaultContext, ILoop
{
	public static final Name	$forStart	= Name.getQualified("$forStart");
	public static final Name	$forUpdate	= Name.getQualified("$forCondition");
	public static final Name	$forEnd		= Name.getQualified("$forEnd");
	
	public Variable variable;
	
	public IValue	condition;
	public IValue	update;
	
	public IValue action;
	
	protected Label	startLabel;
	protected Label	updateLabel;
	protected Label	endLabel;
	
	public ForStatement(Variable variable, IValue condition, IValue update, IValue action)
	{
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
		
		this.variable = variable;
		this.condition = condition;
		this.update = update;
		this.action = action;
	}
	
	@Override
	public int valueTag()
	{
		return FOR;
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable = new Variable(type.getPosition());
		this.variable.type = type;
	}
	
	@Override
	public Label getContinueLabel()
	{
		return this.updateLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variable != null && this.variable.name == name)
		{
			return this.variable;
		}
		
		return null;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (name == $forStart)
		{
			return this.startLabel;
		}
		if (name == $forUpdate)
		{
			return this.updateLabel;
		}
		if (name == $forEnd)
		{
			return this.endLabel;
		}
		
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.resolveTypes(markers, context);
		}
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.update != null)
		{
			this.update.resolveTypes(markers, context);
		}
		
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.action != null)
		{
			this.action.resolveStatement(new CombiningLabelContext(this, context), markers);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		if (this.variable != null)
		{
			this.variable.resolve(markers, context);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context1);
		}
		if (this.update != null)
		{
			this.update = this.update.resolve(markers, context1);
		}
		
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context1);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.checkTypes(markers, context);
		}
		IContext context1 = new CombiningContext(this, context);
		if (this.update != null)
		{
			this.update.checkTypes(markers, context1);
		}
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN, Types.BOOLEAN, markers, context);
			if (condition1 == null)
			{
				Marker marker = markers.create(this.condition.getPosition(), "for.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
			}
			else
			{
				this.condition = condition1;
			}
			
			this.condition.checkTypes(markers, context1);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context1);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		
		if (this.variable != null)
		{
			this.variable.check(markers, context);
		}
		if (this.update != null)
		{
			this.update.check(markers, context1);
		}
		if (this.condition != null)
		{
			this.condition.check(markers, context1);
		}
		if (this.action != null)
		{
			this.action.check(markers, context1);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.variable != null)
		{
			this.variable.foldConstants();
		}
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.update != null)
		{
			this.update = this.update.foldConstants();
		}
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		IContext context1 = new CombiningContext(this, context);
		
		if (this.variable != null)
		{
			this.variable.cleanup(context1, compilableList);
		}
		if (this.update != null)
		{
			this.update.cleanup(context1, compilableList);
		}
		if (this.condition != null)
		{
			this.condition.cleanup(context1, compilableList);
		}
		if (this.action != null)
		{
			this.action.cleanup(context1, compilableList);
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
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		Variable var = this.variable;
		
		int locals = writer.localCount();
		// Variable
		if (var != null)
		{
			var.writeInit(writer, var.value);
		}
		writer.writeTargetLabel(startLabel);
		// Condition
		if (this.condition != null)
		{
			this.condition.writeInvJump(writer, endLabel);
		}
		// Then
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		// Update
		writer.writeLabel(updateLabel);
		if (this.update != null)
		{
			this.update.writeStatement(writer);
		}
		// Go back to Condition
		writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		// Variable
		if (var != null)
		{
			var.writeLocal(writer, startLabel, endLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		if (this.variable != null)
		{
			this.variable.toString(prefix, buffer);
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
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
