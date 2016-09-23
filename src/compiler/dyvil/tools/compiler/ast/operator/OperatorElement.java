package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OperatorElement
{
	public final Name          name;
	public final ICodePosition position;
	protected    IOperator     operator;

	public OperatorElement(Name name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
	}

	protected void resolve(MarkerList markers, IContext context)
	{
		IOperator operator = IContext.resolveOperator(context, this.name, IOperator.INFIX);

		if (operator != null)
		{
			this.operator = operator;

			// Infix check
			checkPosition(markers, this.position, operator, IOperator.INFIX);
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
		operator = IContext.resolveOperator(context, removeEq, IOperator.INFIX);

		if (operator == null)
		{
			this.operator = Operator.DEFAULT_RIGHT;
			return;
		}
		if (operator.getAssociativity() != IOperator.RIGHT)
		{
			this.operator = new Operator(removeEq, IOperator.RIGHT, operator.getPrecedence() - 1);
			return;
		}

		// No infix check required, the type is ID_INFIX_RIGHT
		this.operator = operator;
	}

	public static void checkPosition(MarkerList markers, ICodePosition position, IOperator operator, int expectedType)
	{
		final byte operatorType = operator.getType();
		if (operatorType != expectedType && !(operatorType == IOperator.TERNARY && expectedType == IOperator.INFIX))
		{
			final Marker marker = Markers.semantic(position, "operator.invalid_position", operator.getName(),
			                                       typeToString(operatorType), typeToString(expectedType));
			marker.addInfo(Markers.getSemantic("operator.declaration", operator.toString()));
			markers.add(marker);
		}
	}

	private static String typeToString(int type)
	{
		switch (type)
		{
		case IOperator.INFIX:
			return "infix";
		case IOperator.PREFIX:
			return "prefix";
		case IOperator.POSTFIX:
			return "postfix";
		case IOperator.TERNARY:
			return "ternary";
		}
		return null;
	}

	@Override
	public String toString()
	{
		return this.name.toString();
	}
}
