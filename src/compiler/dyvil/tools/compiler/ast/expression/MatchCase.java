package dyvil.tools.compiler.ast.expression;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.CombiningContext;
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
	
	protected Label switchLabel;
	
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
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.resolveTypes(markers, new CombiningContext(this, context));
		}
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
		
		final IContext combinedContext = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, combinedContext);
			this.condition = IStatement.checkCondition(markers, context, this.condition, "match.condition.type");
		}
		
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, combinedContext);
		}
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition.checkTypes(markers, context1);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, context1);
		}
	}
	
	public void check(MarkerList markers, IContext context)
	{
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition.check(markers, context1);
		}
		if (this.action != null)
		{
			this.action.check(markers, context1);
		}
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
		IContext context1 = new CombiningContext(this, context);
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context1, compilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(context1, compilableList);
		}
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
