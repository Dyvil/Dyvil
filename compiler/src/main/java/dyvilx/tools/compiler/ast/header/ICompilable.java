package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface ICompilable
{
	String getFileName();

	String getInternalName();

	String getFullName();

	void write(ClassWriter writer) throws BytecodeException;
}
