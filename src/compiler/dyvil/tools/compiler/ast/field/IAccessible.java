package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IAccessible
{
	IType getType();
	
	void writeGet(MethodWriter writer) throws BytecodeException;
}
