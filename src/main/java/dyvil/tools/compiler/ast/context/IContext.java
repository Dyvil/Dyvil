package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.classes.IClass;

public interface IContext
{
	public IClass resolveClass(String name);
}
