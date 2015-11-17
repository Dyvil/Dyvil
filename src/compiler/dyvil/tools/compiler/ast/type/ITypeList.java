package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;

public interface ITypeList extends ITypeConsumer
{
	int typeCount();
	
	void setType(int index, IType type);
	
	void addType(IType type);
	
	IType getType(int index);
	
	@Override
	default void setType(IType type)
	{
		this.addType(type);
	}
}
