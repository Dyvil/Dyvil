package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IClassCompilable
{
	public default boolean hasSeparateFile()
	{
		return false;
	}
	
	public default String getFileName()
	{
		throw new UnsupportedOperationException();
	}
	
	public default void setInnerIndex(String internalName, int index)
	{
	}
	
	public void write(ClassWriter writer) throws BytecodeException;
	
	public default void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}
}
