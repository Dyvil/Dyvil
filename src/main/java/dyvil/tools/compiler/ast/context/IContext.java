package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;

public interface IContext
{
	public boolean isStatic();
	
	public IClass resolveClass(String name);
	
	public IField resolveField(String name);
	
	public IMethod resolveMethodName(String name);
	
	public IMethod resolveMethod(String name, Type... args);
}
