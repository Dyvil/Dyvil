package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.pattern.ICase;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public class MatchCase implements ICase, IDefaultContext
{
	protected IPattern pattern;
	protected IValue   condition;
	protected IValue   action;

	@Override
	public IPattern getPattern()
	{
		return this.pattern;
	}
	
	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}
	
	@Override
	public IValue getCondition()
	{
		return this.condition;
	}
	
	@Override
	public void setCondition(IValue condition)
	{
		this.condition = condition;
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
	
	public boolean isExhaustive()
	{
		return this.pattern.isExhaustive() && this.condition == null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		final IDataMember field = this.pattern.resolveField(name);
		return field != null ? field : null;
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
		context.pop();
	}
	
	public void resolve(MarkerList markers, IType type, IContext context)
	{
		if (this.pattern != null)
		{
			this.pattern = this.pattern.resolve(markers, context);

			final IPattern typedPattern = this.pattern.withType(type, markers);
			if (typedPattern == null)
			{
				Marker marker = Markers.semanticError(this.pattern.getPosition(), "pattern.type.incompatible");
				marker.addInfo(Markers.getSemantic("pattern.type", this.pattern.getType()));
				marker.addInfo(Markers.getSemantic("value.type", type));
				markers.add(marker);
			}
			else
			{
				this.pattern = typedPattern;
			}
		}

		context = context.push(this);

		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
			this.condition = IStatement.checkCondition(markers, context, this.condition, "match.condition.type");
		}
		
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
		}

		context.pop();
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, context
			);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context);
		}
		context.pop();
	}
	
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);
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
	
	public void foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
	}
	
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		context = context.push(this);
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context, compilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(context, compilableList);
		}
		context.pop();
	}

	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		this.toString("", stringBuilder);
		return stringBuilder.toString();
	}

	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("case ");
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
		}
		if (this.condition != null)
		{
			buffer.append(" if ");
			this.condition.toString(prefix, buffer);
		}

		if (Formatting.getBoolean("case.separator.space_before"))
		{
			buffer.append(' ');
		}

		if (Formatting.getBoolean("case.separator.arrow"))
		{
			buffer.append("=>");
		}
		else
		{
			buffer.append(':');
		}

		if (Formatting.getBoolean("case.separator.space_after"))
		{
			buffer.append(' ');
		}

		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
