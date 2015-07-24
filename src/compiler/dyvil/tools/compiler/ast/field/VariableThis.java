package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class VariableThis implements IAccessible
{
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
	public void writeGet(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, this.index);
	}
}
