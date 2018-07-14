package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface IClassCompilable
{
	void write(ClassWriter writer) throws BytecodeException;

	default void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	default void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}
}
