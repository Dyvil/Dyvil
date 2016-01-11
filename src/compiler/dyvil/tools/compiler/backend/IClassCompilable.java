package dyvil.tools.compiler.backend;

import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IClassCompilable
{
	default boolean hasSeparateFile()
	{
		return false;
	}
	
	default String getFileName()
	{
		throw new UnsupportedOperationException();
	}
	
	default void setInnerIndex(String internalName, int index)
	{
	}
	
	void write(ClassWriter writer) throws BytecodeException;
	
	default void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	default void writeInit(MethodWriter writer) throws BytecodeException
	{
	}
}
