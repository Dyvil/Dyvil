package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IParameter extends IVariable, IClassMember
{
	@Override
	public default void setTheClass(IClass iclass)
	{
	}
	
	@Override
	public default IClass getTheClass()
	{
		return null;
	}
	
	public default void setMethod(ICallableMember method)
	{
	}
	
	public int getIndex();
	
	public void setIndex(int index);
	
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
	
	public void write(MethodWriter mw);
}
