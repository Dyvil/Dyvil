package dyvilx.tools.compiler.ast.field;

import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface IAccessible
{
	IType getType();

	void writeGet(MethodWriter writer) throws BytecodeException;
}
