package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;

public interface IMethodSignature extends ITyped, ITypeList, IExceptionList, IGeneric
{
	@Override
	default int typeCount()
	{
		return 0;
	}
	
	@Override
	default void setType(int index, IType type)
	{
	}
	
	@Override
	default IType getType(int index)
	{
		return null;
	}
	
	@Override
	void setType(IType type);
}
