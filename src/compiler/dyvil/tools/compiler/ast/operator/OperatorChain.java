package dyvil.tools.compiler.ast.operator;

import dyvil.collection.Stack;
import dyvil.collection.mutable.LinkedList;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ColonOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
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

		for (int i = 0; i < this.operatorCount; i++)
		{
			this.operators[i].resolve(markers, context);
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
			return binaryOp(this.operands[0], this.operators[0], this.operands[1]).resolve(markers, context);
		case 2:
			final IValue lhs = this.operands[0];
			final OperatorElement element1 = this.operators[0];
			final IValue center = this.operands[1];
			final OperatorElement element2 = this.operators[1];
			final IValue rhs = this.operands[2];

			if (element1.operator.getType() == IOperator.TERNARY && element2.name == element1.operator.getTernaryName())
			{
				return ternaryOp(lhs, element1, center, element2, rhs).resolve(markers, context);
			}
			if (lowerPrecedence(element1, element2, null, markers))
			{
				return binaryOp(lhs, element1, binaryOp(center, element2, rhs)).resolve(markers, context);
			}
			return binaryOp(binaryOp(lhs, element1, center), element2, rhs).resolve(markers, context);
		}

		final Stack<OperatorElement> operatorStack = new LinkedList<>();
		final Stack<IValue> operandStack = new LinkedList<>();
		operandStack.push(this.operands[0]);

		for (int i = 0; i < this.operatorCount; i++)
		{
			final OperatorElement element = this.operators[i];
			pushOperator(operandStack, operatorStack, element, markers);
			operandStack.push(this.operands[i + 1]);
		}
		while (!operatorStack.isEmpty())
		{
			popOperator(operandStack, operatorStack);
		}

		return operandStack.pop().resolve(markers, context);
	}

	private static void pushOperator(Stack<IValue> operandStack, Stack<OperatorElement> operatorStack, OperatorElement element, MarkerList markers)
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
			popOperator(operandStack, operatorStack);
		}
		operatorStack.push(element);
	}

	private static void popOperator(Stack<IValue> operandStack, Stack<OperatorElement> operatorStack)
	{
		final OperatorElement operatorElement = operatorStack.pop();
		final IValue rhs = operandStack.pop();
		final IValue lhs = operandStack.pop();
		final IValue res;

		final OperatorElement peek = operatorStack.peek();
		if (peek != null && peek.operator.getType() == IOperator.TERNARY // ternary operator
			    && operatorElement.name == peek.operator.getTernaryName()) // right-hand part
		{
			final IValue cond = operandStack.pop();
			operatorStack.pop(); // == peek
			res = ternaryOp(cond, peek, lhs, operatorElement, rhs);
		}
		else
		{
			res = binaryOp(lhs, operatorElement, rhs);
		}

		operandStack.push(res);
	}

	private static boolean lowerPrecedence(OperatorElement element1, OperatorElement element2, OperatorElement ternaryOperator, MarkerList markers)
	{
		if (element1.operator.getType() == IOperator.TERNARY)
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
		if (comparePrecedence == 0)
		{
			switch (element1.operator.getAssociativity())
			{
			case IOperator.NONE:
				markers.add(Markers.semantic(element1.position, "operator.infix_none", element1.name));
				// Fallthrough
			case IOperator.LEFT:
				return true;
			}
		}
		return false;
	}

	private static IValue binaryOp(IValue lhs, OperatorElement operator, IValue rhs)
	{
		final Name name = operator.name;

		if (name == Names.colon)
		{
			return new ColonOperator(operator.position, lhs, rhs);
		}
		if (name == Names.eq)
		{
			final IValue assignment = lhs.toAssignment(rhs, operator.position);
			if (assignment != null)
			{
				return assignment;
			}
		}

		return new InfixCall(operator.position, lhs, name, rhs);
	}

	private static IValue ternaryOp(IValue lhs, OperatorElement operator1, IValue center, OperatorElement operator2, IValue rhs)
	{
		if (operator1.name == Names.qmark && operator2.name == Names.colon)
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
		this.operands[0].toString(prefix, buffer);

		for (int i = 0; i < this.operatorCount; i++)
		{
			buffer.append(' ').append(this.operators[i].name).append(' ');
			this.operands[i + 1].toString(prefix, buffer);
		}
	}
}
