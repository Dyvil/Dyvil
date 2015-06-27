package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IParameter extends IVariable, IClassMember, IClassCompilable
{
	@Override
	public default IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public boolean isField();
	
	@Override
	public boolean isVariable();
	
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
	
	public default void setMethod(ICallableMember method)
	{
	}
	
	public void write(MethodWriter mw);
}
