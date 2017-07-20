package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.constant.VoidValue;
import dyvil.tools.compiler.ast.context.CombiningLabelContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class RepeatStatement extends AbstractValue implements IStatement, ILoop
{
	private static final Name $repeatStart     = Name.fromRaw("$repeatStart");
	private static final Name $repeatCondition = Name.fromRaw("$repeatCondition");
	private static final Name $repeatEnd       = Name.fromRaw("$repeatEnd");

	private static final TypeChecker.MarkerSupplier CONDITION_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"repeat.condition.type");

	protected IValue action;
	protected IValue condition;

	private Label startLabel;
	private Label conditionLabel;
	private Label endLabel;

	public RepeatStatement(SourcePosition position)
	{
		this.position = position;

		this.startLabel = new Label($repeatStart);
		this.conditionLabel = new Label($repeatCondition);
		this.endLabel = new Label($repeatEnd);
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

	@Override
	public void setAction(IValue then)
	{
		this.action = then;
	}

	@Override
	public IValue getAction()
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
		if (name == $repeatStart)
		{
			return this.startLabel;
		}
		if (name == $repeatCondition)
		{
			return this.conditionLabel;
		}
		if (name == $repeatEnd)
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
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
			this.action = IStatement.checkStatement(markers, context, this.action, "repeat.action.type");
		}
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
			this.condition = TypeChecker.convertValue(this.condition, Types.BOOLEAN, null, markers, context,
			                                          CONDITION_MARKER_SUPPLIER);
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
			if (this.condition.valueTag() == BOOLEAN && !this.condition.booleanValue())
			{
				return new VoidValue(this.position);
			}
			this.condition = this.condition.foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.action != null)
		{
			this.action = this.action.cleanup(compilableList, classCompilableList);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvil.tools.asm.Label conditionLabel = this.conditionLabel.getTarget();
		dyvil.tools.asm.Label endLabel = this.endLabel.getTarget();

		// Repeat Block

		writer.visitTargetLabel(startLabel);
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		// Condition
		writer.visitLabel(conditionLabel);
		if (this.condition != null)
		{
			this.condition.writeJump(writer, startLabel);
		}
		else
		{
			writer.visitJumpInsn(Opcodes.GOTO, startLabel);
		}

		writer.visitLabel(endLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("repeat");

		if (this.action != null && !Util.formatStatementList(prefix, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("repeat.indent", prefix);
			if (Formatting.getBoolean("repeat.newline_after"))
			{
				buffer.append('\n').append(actionPrefix);
			}
			else
			{
				buffer.append(' ');
			}

			this.action.toString(prefix, buffer);
		}

		if (this.condition != null)
		{
			if (Formatting.getBoolean("repeat.while.newline_before"))
			{
				buffer.append('\n').append(prefix);
			}
			else if (Formatting.getBoolean("repeat.while.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append("while");

			Formatting.appendSeparator(buffer, "while.open_paren", '(');
			this.condition.toString(prefix, buffer);

			if (Formatting.getBoolean("while.close_paren.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(')');
		}
	}
}
