package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InfixCall extends MethodCall
{
	public InfixCall(ICodePosition position, IValue lhs, Name name, IValue rhs)
	{
		super(position, lhs, name, new SingleArgument(rhs));
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		IValue op = Operators.getInfix_Priority(this.receiver, this.name, this.arguments.getFirstValue());
		if (op != null)
		{
			// Intrinsic Infix Operators (namely ==, ===, != and !== for null)
			op.setPosition(this.position);
			return op.resolveOperator(markers, context);
		}

		// Normal Method Resolution
		if (this.resolveMethodCall(markers, context))
		{
			return this;
		}

		// Infix Operators
		op = Operators.getInfix(this.receiver, this.name, this.arguments.getFirstValue());
		if (op != null)
		{
			op.setPosition(this.position);
			return op.resolveOperator(markers, context);
		}

		// Compound Operators
		if (Util.hasEq(this.name))
		{
			return CompoundCall.resolveCall(markers, context, this.position, this.receiver, Util.removeEq(this.name),
			                                this.arguments);
		}

		// No Implicit or Apply Resolution
		return null;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
		}
		buffer.append(' ').append(this.name.unqualified).append(' ');
		if (!this.arguments.isEmpty())
		{
			this.arguments.getFirstValue().toString(prefix, buffer);
		}
	}
}
