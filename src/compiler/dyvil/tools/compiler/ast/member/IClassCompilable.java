package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IClassCompilable
{
	public default String getFileName()
	{
		throw new UnsupportedOperationException();
	}
	
	public default void setInnerIndex(int index)
	{
	}
	
	public void write(ClassWriter writer) throws BytecodeException;
}
