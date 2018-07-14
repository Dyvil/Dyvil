package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.expression.constant.VoidValue;
import dyvilx.tools.compiler.ast.context.CombiningLabelContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.AbstractValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.statement.IStatement;
import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class WhileStatement extends AbstractValue implements IStatement, ILoop
{
	public static final Name $whileStart = Name.fromRaw("$whileStart");
	public static final Name $whileEnd   = Name.fromRaw("$whileEnd");

	private static final TypeChecker.MarkerSupplier CONDITION_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"while.condition.type");

	protected IValue condition;
	protected IValue action;

	// Metadata
	private Label startLabel;
	private Label endLabel;

	public WhileStatement(SourcePosition position)
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

	@Override
	public void setAction(IValue action)
	{
		this.action = action;
	}

	@Override
	public IValue getAction()
	{
		return this.action;
	}

	@Override
	public int valueTag()
	{
		return WHILE;
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
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
			this.condition = TypeChecker.convertValue(this.condition, Types.BOOLEAN, null, markers, context,
			                                          CONDITION_MARKER_SUPPLIER);
		}
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
			this.action = IStatement.checkStatement(markers, context, this.action, "while.action.type");
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
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
			markers.add(Markers.semantic(this.position, "while.condition.invalid"));
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
			// while (false)
			if (this.condition.valueTag() == BOOLEAN && !this.condition.booleanValue())
			{
				return new VoidValue(this.position);
			}
			this.condition = this.condition.foldConstants();
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
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(compilableList, classCompilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(compilableList, classCompilableList);
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

		return null;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvilx.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvilx.tools.asm.Label endLabel = this.endLabel.getTarget();

		// Condition
		writer.visitTargetLabel(startLabel);
		this.condition.writeInvJump(writer, endLabel);
		// While Block
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		writer.visitJumpInsn(Opcodes.GOTO, startLabel);

		writer.visitLabel(endLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("while");

		Formatting.appendSeparator(buffer, "while.open_paren", '(');
		if (this.condition != null)
		{
			this.condition.toString(prefix, buffer);
		}

		if (Formatting.getBoolean("while.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		if (this.action == null)
		{
			return;
		}

		if (Util.formatStatementList(prefix, buffer, this.action))
		{
			return;
		}

		String valuePrefix = Formatting.getIndent("while.indent", prefix);

		if (Formatting.getBoolean("while.close_paren.newline_after"))
		{
			buffer.append('\n').append(valuePrefix);
		}
		else if (Formatting.getBoolean("while.close_paren.space_after"))
		{
			buffer.append(' ');
		}

		this.action.toString(prefix, buffer);
	}
}
