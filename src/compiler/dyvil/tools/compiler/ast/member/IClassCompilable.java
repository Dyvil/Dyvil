package dyvil.tools.compiler.ast.member;

import jdk.internal.org.objectweb.asm.ClassWriter;

public interface IClassCompilable
{
	public void write(ClassWriter writer);
}
