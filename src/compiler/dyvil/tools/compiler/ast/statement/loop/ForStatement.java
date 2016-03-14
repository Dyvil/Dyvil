package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.CombiningLabelContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ForStatement implements IForStatement, IDefaultContext
{
	public static final Name $forStart  = Name.getQualified("$forStart");
	public static final Name $forUpdate = Name.getQualified("$forCondition");
	public static final Name $forEnd    = Name.getQualified("$forEnd");

	protected ICodePosition position;
	protected IVariable     variable;

	protected IValue condition;
	protected IValue update;

	protected IValue action;

	// Metadata
	protected Label startLabel;
	protected Label updateLabel;
	protected Label endLabel;

	public ForStatement(ICodePosition position, Variable variable, IValue condition, IValue update)
	{
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);

		this.position = position;
		this.variable = variable;
		this.condition = condition;
		this.update = update;
	}

	public ForStatement(ICodePosition position, Variable variable, IValue condition, IValue update, IValue action)
	{
		this(position, variable, condition, update);
		this.action = action;
	}

	@Override
	public int valueTag()
	{
		return FOR;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public IVariable getVariable()
	{
		return this.variable;
	}

	@Override
	public void setVariable(IVariable variable)
	{
		this.variable = variable;
	}

	@Override
	public IValue getAction()
	{
		return this.action;
	}

	@Override
	public void setAction(IValue action)
	{
		this.action = action;
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
		if (this.variable != null && this.variable.getName() == name)
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
		if (this.variable != null)
		{
			this.variable.resolve(markers, context);
		}

		context = context.push(this);
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
			this.condition = IStatement.checkCondition(markers, context, this.condition, "for.condition.type");
		}
		if (this.update != null)
		{
			this.update = this.update.resolve(markers, context);
			this.update = IStatement.checkStatement(markers, context, this.update, "for.update.type");
		}

		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
			this.action = IStatement.checkStatement(markers, context, this.action, "for.action.type");
		}
		context.pop();

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.checkTypes(markers, context);
		}

		context = context.push(this);
		if (this.update != null)
		{
			this.update.checkTypes(markers, context);
		}
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context);
		}
		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.check(markers, context);
		}

		markers.add(Markers.semanticWarning(this.position, "for.deprecated"));

		context = context.push(this);
		if (this.update != null)
		{
			this.update.check(markers, context);
		}
		if (this.condition != null)
		{
			this.condition.check(markers, context);
		}
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
		context.pop();
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
		if (this.variable != null)
		{
			this.variable.cleanup(context, compilableList);
		}

		context = context.push(this);

		if (this.update != null)
		{
			this.update.cleanup(context, compilableList);
		}
		if (this.condition != null)
		{
			this.condition.cleanup(context, compilableList);
		}
		if (this.action != null)
		{
			this.action.cleanup(context, compilableList);
		}

		context.pop();

		return this;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();

		IVariable var = this.variable;

		int locals = writer.localCount();
		// Variable
		if (var != null)
		{
			var.writeInit(writer, var.getValue());
		}
		writer.writeTargetLabel(startLabel);
		// Condition
		if (this.condition != null)
		{
			this.condition.writeInvJump(writer, endLabel);
		}
		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		// Update
		writer.writeLabel(updateLabel);
		if (this.update != null)
		{
			this.update.writeExpression(writer, Types.VOID);
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
		buffer.append("for");
		Formatting.appendSeparator(buffer, "for.open_paren", '(');

		if (this.variable != null)
		{
			this.variable.toString(prefix, buffer);

			if (Formatting.getBoolean("for.variable_semicolon.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append(';');
		if (this.condition != null)
		{
			if (Formatting.getBoolean("for.variable_semicolon.space_after"))
			{
				buffer.append(' ');
			}

			this.condition.toString(prefix, buffer);

			if (Formatting.getBoolean("for.condition_semicolon.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append(';');

		if (this.update != null)
		{
			if (Formatting.getBoolean("for.condition_semicolon.space_after"))
			{
				buffer.append(' ');
			}

			this.update.toString(prefix, buffer);
		}

		if (Formatting.getBoolean("for.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		if (this.action != null && !Util.formatStatementList(prefix, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("for.indent", prefix);
			if (Formatting.getBoolean("for.close_paren.newline_after"))
			{
				buffer.append('\n').append(actionPrefix);
			}
			else if (Formatting.getBoolean("for.close_paren.space_after"))
			{
				buffer.append(' ');
			}

			this.action.toString(actionPrefix, buffer);
		}
	}
}
