package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IClassCompilable
{
	public void write(ClassWriter writer) throws BytecodeException;
}
