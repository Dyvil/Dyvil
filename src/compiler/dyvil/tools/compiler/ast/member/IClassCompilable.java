package dyvil.tools.compiler.ast.member;

import org.objectweb.asm.ClassWriter;

public interface IClassCompilable
{
	public void write(ClassWriter writer);
}
