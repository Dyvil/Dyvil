package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;

public interface IClassContext extends IContext
{
	public IField resolveField(String name);
	
	public IMethod resolveMethod(String name, Type... args);
}
