package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OperatorChain implements IValue
{
	private Name[]          operators         = new Name[2];
	private ICodePosition[] operatorPositions = new ICodePosition[2];
	private IValue[]        operands          = new IValue[3];
	private int operatorCount;

	@Override
	public int valueTag()
	{
		return OPERATOR_CHAIN;
	}

	public void addOperator(Name operator, ICodePosition position)
	{
		final int index = this.operatorCount++;
		if (index >= this.operators.length)
		{
			final Name[] tempOperators = new Name[index + 1];
			System.arraycopy(this.operators, 0, tempOperators, 0, this.operators.length);
			this.operators = tempOperators;

			final ICodePosition[] tempPositions = new ICodePosition[index + 1];
			System.arraycopy(this.operatorPositions, 0, tempPositions, 0, this.operators.length);
			this.operatorPositions = tempPositions;
		}
		this.operators[index] = operator;
		this.operatorPositions[index] = position;
	}

	public void addOperand(IValue value)
	{
		final int index = this.operatorCount;
		if (index >= this.operands.length)
		{
			final IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.operands, 0, temp, 0, this.operands.length);
			this.operands = temp;
		}
		this.operands[index] = value;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public ICodePosition getPosition()
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
		System.out.println(this);

		Name operatorName = this.operators[0];
		Operator operator = null; // TODO Resolve Operator
		IValue operand = this.operands[0];

		for (int i = 1; i < this.operatorCount; i++)
		{

		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		type.writeDefaultValue(writer);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('(');
		this.operands[0].toString(prefix, buffer);

		for (int i = 0; i < this.operatorCount; i++)
		{
			buffer.append(") ").append(this.operators[i]).append(" (");
			this.operands[i + 1].toString(prefix, buffer);
		}

		buffer.append(')');
	}
}
