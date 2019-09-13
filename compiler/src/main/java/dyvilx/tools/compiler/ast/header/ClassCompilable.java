package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;

public interface ClassCompilable
{
	void write(ClassWriter writer) throws BytecodeException;

	default void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	default void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}
}
