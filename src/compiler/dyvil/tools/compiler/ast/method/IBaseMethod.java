package dyvil.tools.compiler.ast.method;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValued;

public interface IBaseMethod extends IASTNode, IValued, IParameterized, IContext
{

	public void write(ClassWriter writer);
}
