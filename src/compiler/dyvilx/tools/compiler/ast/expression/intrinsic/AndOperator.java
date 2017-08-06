package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class AndOperator extends BooleanOperator
{
	public AndOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}

	public AndOperator(SourcePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	public int valueTag()
	{
		return BOOLEAN_AND;
	}

	@Override
	protected IValue optimize()
	{
		if (hasValue(this.left, false) || hasValue(this.right, false))
		{
			return BooleanValue.FALSE;
		}
		if (hasValue(this.left, true))
		{
			return this.right;
		}
		if (hasValue(this.right, true))
		{
			return this.left;
		}

		return this;
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		// jump if both conditions are met
		final Label label = new Label();
		this.left.writeInvJump(writer, label);
		this.right.writeJump(writer, dest);
		writer.visitLabel(label);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		// !(x && y) <=> !x || !y
		// jump if either condition is not met
		this.left.writeInvJump(writer, dest);
		this.right.writeInvJump(writer, dest);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" && ");
		this.right.toString(prefix, buffer);
	}
}
