package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.CombiningLabelContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class WhileStatement extends AbstractValue implements IStatement, ILoop
{
	public static final Name $whileStart = Name.getQualified("$whileStart");
	public static final Name $whileEnd   = Name.getQualified("$whileEnd");
	
	protected IValue condition;
	protected IValue action;
	
	// Metadata
	private Label startLabel;
	private Label endLabel;
	
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
			this.condition = IStatement.checkCondition(markers, context, this.condition, "while.condition.type");
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context, compilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(context, compilableList);
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
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		// Condition
		writer.writeTargetLabel(startLabel);
		this.condition.writeInvJump(writer, endLabel);
		// While Block
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		
		writer.writeLabel(endLabel);
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
