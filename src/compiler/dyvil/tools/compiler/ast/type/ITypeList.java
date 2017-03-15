package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;

public interface ITypeList extends ITypeConsumer
{
	int typeCount();

	IType getType(int index);

	IType[] getTypes();

	void setType(int index, IType type);

	void setTypes(IType[] types, int size);

	void addType(IType type);

	@Override
	default void setType(IType type)
	{
		this.addType(type);
	}
}
