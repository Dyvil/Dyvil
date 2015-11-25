package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class DoStatement extends AbstractValue implements IStatement, ILoop
{
	private static final Name $doStart     = Name.getQualified("$doStart");
	private static final Name $doCondition = Name.getQualified("$doCondition");
	private static final Name $doEnd       = Name.getQualified("$doEnd");
	
	protected IValue action;
	protected IValue condition;
	
	// Metadata
	private Label startLabel;
	private Label conditionLabel;
	private Label endLabel;
	
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
	
	public void setAction(IValue then)
	{
		this.action = then;
	}
	
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
			IValue condition1 = this.condition.withType(Types.BOOLEAN, Types.BOOLEAN, markers, context);
			if (condition1 == null)
			{
				Marker marker = I18n.createMarker(this.condition.getPosition(), "do.condition.type");
				marker.addInfo(I18n.getString("value.type", this.condition.getType()));
				markers.add(marker);
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
			if (this.condition.valueTag() == BOOLEAN && !this.condition.booleanValue())
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
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.action == null)
		{
			this.condition.writeExpression(writer, Types.VOID);
		}
		
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label conditionLabel = this.conditionLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		// Do Block
		
		writer.writeTargetLabel(startLabel);
		this.action.writeExpression(writer, Types.VOID);
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
		buffer.append("do");

		if (this.action != null && !Util.formatStatementList(prefix, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("do.indent", prefix);
			if (Formatting.getBoolean("do.newline_after"))
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
			if (Formatting.getBoolean("do.while.newline_before"))
			{
				buffer.append('\n').append(prefix);
			}
			else if (Formatting.getBoolean("do.while.space_before"))
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
