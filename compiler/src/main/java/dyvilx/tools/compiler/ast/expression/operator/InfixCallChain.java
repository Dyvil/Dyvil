package dyvilx.tools.compiler.ast.expression.operator;

import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ColonOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class InfixCallChain extends OperatorStack<IValue> implements IValue
{
	@Override
	public int valueTag()
	{
		return OPERATOR_CHAIN;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}

	@Override
	public IType getType()
	{
		return Types.UNKNOWN;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i <= this.operatorCount; i++)
		{
			((IValue) this.operands[i]).resolveTypes(markers, context);
		}

		this.resolveOperators(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this.treeify(markers).resolve(markers, context);
	}

	@Override
	protected IValue binaryOp(IValue lhs, OperatorElement operator, IValue rhs)
	{
		final Name name = operator.name;

		if (name == Names.$colon)
		{
			return new ColonOperator(operator.position, lhs, rhs);
		}
		if (name == Names.$eq)
		{
			final IValue assignment = lhs.toAssignment(rhs, operator.position);
			if (assignment != null)
			{
				return assignment;
			}
		}

		return new InfixCall(operator.position, lhs, name, rhs);
	}

	@Override
	protected IValue ternaryOp(IValue lhs, OperatorElement operator1, IValue center, OperatorElement operator2,
		                          IValue rhs)
	{
		if (operator1.name == Names.$qmark && operator2.name == Names.$colon)
		{
			return new IfStatement(lhs, center, rhs);
		}

		return new MethodCall(operator1.position, lhs, operator1.name, new ArgumentList(center, rhs));
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		type.writeDefaultValue(writer);
	}
}
