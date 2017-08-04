package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public class MatchCase implements IResolvable, IDefaultContext, IPatternConsumer
{
	private static final TypeChecker.MarkerSupplier CONDITION_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"match.condition.type");

	protected IPattern pattern;
	protected IValue   condition;
	protected IValue   action;

	public IPattern getPattern()
	{
		return this.pattern;
	}

	@Override
	public void setPattern(IPattern pattern)
	{
		this.pattern = pattern;
	}

	public IValue getCondition()
	{
		return this.condition;
	}

	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}

	public IValue getAction()
	{
		return this.action;
	}

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

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.resolveField(variable.getName()) == variable;
	}

	@Override
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

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.resolve(markers, null, context);
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
			this.condition = TypeChecker.convertValue(this.condition, Types.BOOLEAN, null, markers, context,
			                                          CONDITION_MARKER_SUPPLIER);
		}

		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);
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

	@Override
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

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(compilableList, classCompilableList);
		}
		if (this.action != null)
		{
			this.action = this.action.cleanup(compilableList, classCompilableList);
		}
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
