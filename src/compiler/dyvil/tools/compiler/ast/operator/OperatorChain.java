package dyvil.tools.compiler.ast.operator;

import dyvil.collection.Stack;
import dyvil.collection.mutable.LinkedList;
import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Util;
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
			System.arraycopy(this.operatorPositions, 0, tempPositions, 0, this.operatorPositions.length);
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
		for (int i = 0; i <= this.operatorCount; i++)
		{
			this.operands[i].resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.operatorCount == 0)
		{
			return this.operands[0];
		}

		final Stack<Integer> operatorStack = new LinkedList<>();
		final Stack<IValue> operandStack = new LinkedList<>();
		operandStack.push(this.operands[0]);

		for (int i = 0; i < this.operatorCount; i++)
		{
			final Operator operator = resolveOperator(context, this.operators[i]);
			int index;
			Operator operator2;

			while (!operatorStack.isEmpty())
			{
				index = operatorStack.peek();
				operator2 = resolveOperator(context, this.operators[index]);
				final int comparePrecedence = operator.comparePrecedence(operator2);

				if (!operator.isRightAssociative() && 0 == comparePrecedence || comparePrecedence < 0)
				{
					operatorStack.pop();
					this.pushCall(operandStack, index);
				}
				else
				{
					break;
				}
			}
			operatorStack.push(i);
			operandStack.push(this.operands[i + 1]);
		}
		while (!operatorStack.isEmpty())
		{
			this.pushCall(operandStack, operatorStack.pop());
		}

		return operandStack.pop().resolve(markers, context);
	}

	private static Operator resolveOperator(IContext context, Name name)
	{
		Operator operator = IContext.resolveOperator(context, name);

		if (operator != null)
		{
			return operator;
		}

		if (!Util.hasEq(name))
		{
			return Operator.DEFAULT;
		}

		final Name removeEq = Util.removeEq(name);
		operator = IContext.resolveOperator(context, removeEq);

		if (operator == null)
		{
			return Operator.DEFAULT_RIGHT;
		}
		if (!operator.isRightAssociative())
		{
			return new Operator(removeEq, operator.precedence - 1, Operator.INFIX_RIGHT);
		}
		return operator;
	}

	private void pushCall(Stack<IValue> stack, int index)
	{
		final IValue rhs = stack.pop();
		final IValue lhs = stack.pop();

		final Name operator = this.operators[index];
		final ICodePosition position = this.operatorPositions[index];

		if (operator == Names.colon)
		{
			stack.push(new ColonOperator(position, lhs, rhs));
			return;
		}
		if (operator == Names.eq)
		{
			final IValue assignment = lhs.toAssignment(rhs, position);
			if (assignment != null)
			{
				stack.push(assignment);
				return;
			}
		}

		final MethodCall methodCall = new MethodCall(position, lhs, operator, new SingleArgument(rhs));
		methodCall.setDotless(true);
		stack.push(methodCall);
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
