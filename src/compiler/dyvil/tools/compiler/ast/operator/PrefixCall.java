package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class PrefixCall extends MethodCall
{
	public PrefixCall(ICodePosition position, Name name)
	{
		super(position, null, name);
		this.dotless = true;
	}

	public PrefixCall(ICodePosition position, Name name, IValue argument)
	{
		super(position, null, name, new SingleArgument(argument));
		this.dotless = true;
	}

	@Override
	public int valueTag()
	{
		return PREFIX_CALL;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		final IOperator operator = IContext.resolveOperator(context, this.name, IOperator.PREFIX);
		if (operator == null)
		{
			markers.add(Markers.semantic(this.position, "operator.unresolved", this.name));
		}
		else
		{
			OperatorElement.checkPosition(markers, this.position, operator, IOperator.PREFIX);
		}

		super.resolveTypes(markers, context);
	}

	@Override
	public IValue toAssignment(IValue rhs, ICodePosition position)
	{
		final Name name = Name.get(this.name.unqualified + "_=", this.name.qualified + "_$eq");
		return new MethodCall(this.position, this.arguments.getFirstValue(), name, new SingleArgument(rhs));
	}
}
