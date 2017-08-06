package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class PopExpr extends UnaryOperator
{
	public PopExpr(IValue value)
	{
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return POP_EXPR;
	}

	@Override
	public boolean isIgnoredClassAccess()
	{
		return true;
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		return this;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final IType valueType = this.value.getType();
		this.value.writeExpression(writer, null);

		if (!Types.isVoid(valueType))
		{
			writer.visitInsn(Opcodes.AUTO_POP);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("__discard__ ");
		this.value.toString(prefix, buffer);
	}
}
