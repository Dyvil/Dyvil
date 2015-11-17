package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IParameter extends IVariable, IClassMember
{
	@Override
	default void setTheClass(IClass iclass)
	{
	}
	
	@Override
	default IClass getTheClass()
	{
		return null;
	}
	
	default void setMethod(ICallableMember method)
	{
	}
	
	int getIndex();
	
	void setIndex(int index);
	
	@Override
	boolean isField();
	
	@Override
	boolean isVariable();
	
	default void setVarargs(boolean varargs)
	{
	
	}
	
	default boolean isVarargs()
	{
		return false;
	}
	
	void write(MethodWriter mw);
}
