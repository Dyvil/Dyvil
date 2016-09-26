package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

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
