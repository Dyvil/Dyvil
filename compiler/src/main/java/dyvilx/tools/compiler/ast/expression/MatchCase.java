package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class MatchCase implements Resolvable, IDefaultContext
{
	// =============== Constants ===============

	private static final TypeChecker.MarkerSupplier CONDITION_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"match.condition.type");

	// =============== Fields ===============

	protected Pattern pattern;
	protected IValue  condition;
	protected IValue  action;

	// =============== Properties ===============

	public Pattern getPattern()
	{
		return this.pattern;
	}

	public void setPattern(Pattern pattern)
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

	// =============== Methods ===============

	// --------------- Context Resolution ---------------

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.pattern.resolveField(name);
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.resolveField(variable.getName()) == variable;
	}

	// --------------- Resolution Phases ---------------

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

			final Pattern typedPattern = this.pattern.withType(type, markers);
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

	// --------------- Diagnostic Phases ---------------

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

	// --------------- Compilation Phases ---------------

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

	// --------------- Formatting ---------------

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
