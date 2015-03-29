package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IParameter extends IVariable
{
	public void setVarargs(boolean varargs);
	
	public boolean isVarargs();
	
	public default void setTheClass(IClass iclass)
	{
	}
	
	public default void setMethod(IBaseMethod method)
	{
	}
	
	public void write(MethodWriter mw);
}
