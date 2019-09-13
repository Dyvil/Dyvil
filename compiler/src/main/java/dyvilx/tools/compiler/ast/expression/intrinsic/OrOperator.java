package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class OrOperator extends BooleanOperator
{
	public OrOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}

	public OrOperator(SourcePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	protected IValue optimize()
	{
		if (hasValue(this.left, true) || hasValue(this.right, true))
		{
			return BooleanValue.TRUE;
		}
		if (hasValue(this.left, false))
		{
			return this.right;
		}
		if (hasValue(this.right, false))
		{
			return this.left;
		}

		return this;
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		// jump if either condition is met
		this.left.writeJump(writer, dest);
		this.right.writeJump(writer, dest);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		// !(x || y) <=> !x && !y
		// jump if both conditions are not met
		final Label label = new Label();
		this.left.writeJump(writer, label);
		this.right.writeInvJump(writer, dest);
		writer.visitLabel(label);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" || ");
		this.right.toString(prefix, buffer);
	}
}
