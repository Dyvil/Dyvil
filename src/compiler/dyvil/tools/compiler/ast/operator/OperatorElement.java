package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

class OperatorElement
{
	protected final Name          name;
	protected final ICodePosition position;
	protected       Operator      operator;

	public OperatorElement(Name name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
	}

	protected void resolve(MarkerList markers, IContext context)
	{
		Operator operator = IContext.resolveOperator(context, this.name);

		if (operator != null)
		{
			this.operator = operator;

			// Infix check
			if (!operator.isInfix())
			{
				final Marker marker = Markers.semantic(this.position, "operator.not_infix", this.name);
				marker.addInfo(Markers.getSemantic("operator.declaration", operator.toString()));
				markers.add(marker);
			}
			return;
		}

		if (!Util.hasEq(this.name))
		{
			// Unresolved operator, no =
			this.operator = Operator.DEFAULT;
			markers.add(Markers.semantic(this.position, "operator.unresolved", this.name));
			return;
		}

		// = at the end
		final Name removeEq = Util.removeEq(this.name);
		operator = IContext.resolveOperator(context, removeEq);

		if (operator == null)
		{
			this.operator = Operator.DEFAULT_RIGHT;
			return;
		}
		if (!operator.isRightAssociative())
		{
			this.operator = new Operator(removeEq, operator.precedence - 1, Operator.INFIX_RIGHT);
			return;
		}

		// No infix check required, the type is INFIX_RIGHT
		this.operator = operator;
	}
}
