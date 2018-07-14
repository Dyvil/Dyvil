package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.context.CombiningLabelContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.statement.IStatement;
import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ForStatement implements IForStatement, IDefaultContext
{
	public static final Name $forStart  = Name.fromRaw("$forStart");
	public static final Name $forUpdate = Name.fromRaw("$forCondition");
	public static final Name $forEnd    = Name.fromRaw("$forEnd");

	private static final TypeChecker.MarkerSupplier CONDITION_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"for.condition.type");

	protected SourcePosition position;
	protected IVariable     variable;

	protected IValue condition;
	protected IValue update;

	protected IValue action;

	// Metadata
	protected Label startLabel;
	protected Label updateLabel;
	protected Label endLabel;

	public ForStatement(SourcePosition position, IVariable variable, IValue condition, IValue update)
	{
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);

		this.position = position;
		this.variable = variable;
		this.condition = condition;
		this.update = update;
	}

	public ForStatement(SourcePosition position, IVariable variable, IValue condition, IValue update, IValue action)
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
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
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
	public boolean isMember(IVariable variable)
	{
		return variable == this.variable;
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
			this.condition = TypeChecker.convertValue(this.condition, Types.BOOLEAN, null, markers, context,
			                                          CONDITION_MARKER_SUPPLIER);
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
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.variable != null)
		{
			this.variable.cleanup(compilableList, classCompilableList);
		}

		if (this.update != null)
		{
			this.update.cleanup(compilableList, classCompilableList);
		}
		if (this.condition != null)
		{
			this.condition.cleanup(compilableList, classCompilableList);
		}
		if (this.action != null)
		{
			this.action.cleanup(compilableList, classCompilableList);
		}

		return this;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvilx.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvilx.tools.asm.Label updateLabel = this.updateLabel.getTarget();
		dyvilx.tools.asm.Label endLabel = this.endLabel.getTarget();

		IVariable var = this.variable;

		int locals = writer.localCount();
		// Variable
		if (var != null)
		{
			var.writeInit(writer);
		}
		writer.visitTargetLabel(startLabel);
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
		writer.visitLabel(updateLabel);
		if (this.update != null)
		{
			this.update.writeExpression(writer, Types.VOID);
		}
		// Go back to Condition
		writer.visitJumpInsn(Opcodes.GOTO, startLabel);
		writer.resetLocals(locals);
		writer.visitLabel(endLabel);
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
