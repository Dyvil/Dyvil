package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
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
