package dyvilx.tools.compiler.ast.expression.operator;

import dyvil.collection.Stack;
import dyvil.collection.mutable.LinkedList;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class OperatorStack<T extends ASTNode> implements ASTNode
{
	protected int               operatorCount;
	protected ASTNode[]        operands = new ASTNode[3];
	protected OperatorElement[] operators = new OperatorElement[2];

	public void addOperator(Name name, SourcePosition position)
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

	public void addOperand(T value)
	{
		final int index = this.operatorCount;
		if (index >= this.operands.length)
		{
			final ASTNode[] temp = new ASTNode[index + 1];
			System.arraycopy(this.operands, 0, temp, 0, this.operands.length);
			this.operands = temp;
		}
		this.operands[index] = value;
	}

	public void resolveOperators(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.operatorCount; i++)
		{
			this.operators[i].resolve(markers, context);
		}
	}

	public T treeify(MarkerList markers)
	{
		switch (this.operatorCount)
		{
		case 0:
			return (T) this.operands[0];
		case 1:
			return this.binaryOp((T) this.operands[0], this.operators[0], (T) this.operands[1]);
		case 2:
			final T lhs = (T) this.operands[0];
			final OperatorElement element1 = this.operators[0];
			final T center = (T) this.operands[1];
			final OperatorElement element2 = this.operators[1];
			final T rhs = (T) this.operands[2];

			if (element1.operator.getType() == IOperator.TERNARY && element2.name == element1.operator.getTernaryName())
			{
				return this.ternaryOp(lhs, element1, center, element2, rhs);
			}
			if (lowerPrecedence(element2, element1, null, markers))
			{
				return this.binaryOp(this.binaryOp(lhs, element1, center), element2, rhs);
			}
			return this.binaryOp(lhs, element1, this.binaryOp(center, element2, rhs));
		}

		final Stack<OperatorElement> operatorStack = new LinkedList<>();
		final Stack<T> operandStack = new LinkedList<>();
		operandStack.push((T) this.operands[0]);

		for (int i = 0; i < this.operatorCount; i++)
		{
			final OperatorElement element = this.operators[i];
			this.pushOperator(operandStack, operatorStack, element, markers);
			operandStack.push((T) this.operands[i + 1]);
		}
		while (!operatorStack.isEmpty())
		{
			this.popOperator(operandStack, operatorStack);
		}

		return operandStack.pop();
	}

	protected void pushOperator(Stack<T> operandStack, Stack<OperatorElement> operatorStack, OperatorElement element,
		                           MarkerList markers)
	{
		OperatorElement element2;
		while (!operatorStack.isEmpty())
		{
			element2 = operatorStack.peek();
			final OperatorElement ternary = operatorStack.peek(1);
			if (!lowerPrecedence(element, element2,
			                     ternary != null && ternary.operator.getType() == IOperator.TERNARY ? ternary : null,
			                     markers))
			{
				break;
			}
			// operatorStack.pop() == element2
			this.popOperator(operandStack, operatorStack);
		}
		operatorStack.push(element);
	}

	protected void popOperator(Stack<T> operandStack, Stack<OperatorElement> operatorStack)
	{
		final OperatorElement operatorElement = operatorStack.pop();
		final T rhs = operandStack.pop();
		final T lhs = operandStack.pop();
		final T res;

		final OperatorElement peek = operatorStack.peek();
		if (peek != null && peek.operator.getType() == IOperator.TERNARY // ternary operator
			    && operatorElement.name == peek.operator.getTernaryName()) // right-hand part
		{
			final T cond = operandStack.pop();
			operatorStack.pop(); // == peek
			res = this.ternaryOp(cond, peek, lhs, operatorElement, rhs);
		}
		else
		{
			res = this.binaryOp(lhs, operatorElement, rhs);
		}

		operandStack.push(res);
	}

	protected static boolean lowerPrecedence(OperatorElement element1, OperatorElement element2,
		                                        OperatorElement ternaryOperator, MarkerList markers)
	{
		final byte element1Type = element1.operator.getType();
		if (element1Type == IOperator.TERNARY)
		{
			if (element2.operator.getType() == IOperator.TERNARY)
			{
				return false;
			}
			if (element2.name == element1.operator.getTernaryName())
			{
				return false;
			}
		}
		if (ternaryOperator != null && element1.name == ternaryOperator.operator.getTernaryName())
		{
			if (element2.name == element1.name)
			{
				return true;
			}
			if (element2.name == ternaryOperator.name)
			{
				return false;
			}
		}

		final int comparePrecedence = element1.operator.comparePrecedence(element2.operator);

		if (comparePrecedence < 0)
		{
			return true;
		}
		if (comparePrecedence > 0)
		{
			return false;
		}

		// Both operators have the same precedence
		switch (element1.operator.getAssociativity())
		{
		case IOperator.NONE:
			if (element1Type != IOperator.INFIX)
			{
				// Prefix and Postfix operators are left-associative when (incorrectly) used in infix position
				return true;
			}
			markers.add(Markers.semanticError(element1.position, "operator.infix_none", element1.name));
			// Fallthrough
		case IOperator.LEFT:
			return true;
		}
		return false;
	}

	protected abstract T binaryOp(T lhs, OperatorElement operator, T rhs);

	protected abstract T ternaryOp(T lhs, OperatorElement operator1, T center, OperatorElement operator2, T rhs);

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.operands[0].toString(prefix, buffer);

		for (int i = 0; i < this.operatorCount; i++)
		{
			buffer.append(' ').append(this.operators[i].name).append(' ');
			this.operands[i + 1].toString(prefix, buffer);
		}
	}
}
