package dyvil.tools.compiler.ast.operator;

import dyvil.collection.Stack;
import dyvil.collection.mutable.LinkedList;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ColonOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OperatorChain implements IValue
{
	private OperatorElement[] operators = new OperatorElement[2];
	private IValue[]          operands  = new IValue[3];
	private int operatorCount;

	@Override
	public int valueTag()
	{
		return OPERATOR_CHAIN;
	}

	public void addOperator(Name name, ICodePosition position)
	{
		final int index = this.operatorCount++;
		if (index >= this.operators.length)
		{
			final OperatorElement[] tempOperators = new OperatorElement[index + 1];
			System.arraycopy(this.operators, 0, tempOperators, 0, this.operators.length);
			this.operators = tempOperators;
		}
		this.operators[index] = new OperatorElement(name, position);
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
		switch (this.operatorCount)
		{
		case 0:
			return this.operands[0];
		case 1:
			this.operators[0].resolve(markers, context);
			return createCall(this.operators[0], this.operands[0], this.operands[1]).resolve(markers, context);
		// TODO Inline Operator resolution for 2 operators?
		}

		for (int i = 0; i < this.operatorCount; i++)
		{
			this.operators[i].resolve(markers, context);
		}

		final Stack<OperatorElement> operatorStack = new LinkedList<>();
		final Stack<IValue> operandStack = new LinkedList<>();
		operandStack.push(this.operands[0]);

		for (int i = 0; i < this.operatorCount; i++)
		{
			final OperatorElement element1 = this.operators[i];
			OperatorElement element2;
			while (!operatorStack.isEmpty())
			{
				element2 = operatorStack.peek();

				final int comparePrecedence = element1.operator.comparePrecedence(element2.operator);
				if (comparePrecedence < 0
					    || element1.operator.getAssociativity() != IOperator.RIGHT && comparePrecedence == 0)
				{
					operatorStack.pop();
					this.pushCall(operandStack, element2);
				}
				else
				{
					break;
				}
			}
			operatorStack.push(element1);
			operandStack.push(this.operands[i + 1]);
		}
		while (!operatorStack.isEmpty())
		{
			this.pushCall(operandStack, operatorStack.pop());
		}

		return operandStack.pop().resolve(markers, context);
	}

	private void pushCall(Stack<IValue> stack, OperatorElement element)
	{
		final IValue rhs = stack.pop();
		final IValue lhs = stack.pop();
		stack.push(createCall(element, lhs, rhs));
	}

	private static IValue createCall(OperatorElement element, IValue lhs, IValue rhs)
	{
		final Name operator = element.name;

		if (operator == Names.colon)
		{
			return new ColonOperator(element.position, lhs, rhs);
		}
		if (operator == Names.eq)
		{
			final IValue assignment = lhs.toAssignment(rhs, element.position);
			if (assignment != null)
			{
				return assignment;
			}
		}

		final MethodCall methodCall = new MethodCall(element.position, lhs, operator, new SingleArgument(rhs));
		methodCall.setDotless(true);
		return methodCall;
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
