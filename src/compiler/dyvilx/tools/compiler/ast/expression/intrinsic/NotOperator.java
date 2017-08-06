package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public final class NotOperator extends UnaryOperator
{
	public NotOperator(IValue right)
	{
		this.value = right;
	}

	public NotOperator(SourcePosition position, IValue right)
	{
		this.position = position;
		this.value = right;
	}

	@Override
	public int valueTag()
	{
		return BOOLEAN_NOT;
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}

	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		if (this.value.valueTag() == BOOLEAN)
		{
			return new BooleanValue(!this.value.booleanValue());
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		BooleanOperator.writeExpression(writer, this, type);
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeInvJump(writer, dest);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeJump(writer, dest);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('!');
		this.value.toString(prefix, buffer);
	}
}
