package dyvilx.tools.compiler.ast.field;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class VariableThis implements IAccessible
{
	public static final VariableThis DEFAULT = new VariableThis();

	private final int index;

	public VariableThis()
	{
		this.index = 0;
	}

	public VariableThis(int index)
	{
		this.index = index;
	}

	@Override
	public IType getType()
	{
		return null;
	}

	@Override
	public void writeGet(MethodWriter writer) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.AUTO_LOAD, this.index);
	}
}
