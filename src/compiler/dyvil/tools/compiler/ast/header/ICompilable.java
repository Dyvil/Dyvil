package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface ICompilable
{
	String getFileName();

	String getFullName();

	void write(ClassWriter writer) throws BytecodeException;
}
