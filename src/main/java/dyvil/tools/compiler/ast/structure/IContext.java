package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;

public interface IContext
{
	public boolean isStatic();
	
	/**
	 * Returns the type of this context. {@code null} in case of a package or
	 * compilation unit, this in case of a Class and the class this is contained
	 * in in case of a method.
	 * 
	 * @return the type of this context
	 */
	public Type getThisType();
	
	public IClass resolveClass(String name);
	
	public IField resolveField(String name);
	
	public IMethod resolveMethod(String name, Type... args);
}
