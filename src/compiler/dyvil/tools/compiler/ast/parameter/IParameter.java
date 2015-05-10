package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IParameter extends IVariable
{
	public default void setVarargs(boolean varargs)
	{
		
	}
	
	public default boolean isVarargs()
	{
		return false;
	}
	
	public default void setTheClass(IClass iclass)
	{
	}
	
	public default void setMethod(IBaseMethod method)
	{
	}
	
	public void write(MethodWriter mw);
}
