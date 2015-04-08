package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.backend.ClassWriter;

public interface IClassCompilable
{
	public void write(ClassWriter writer);
}
