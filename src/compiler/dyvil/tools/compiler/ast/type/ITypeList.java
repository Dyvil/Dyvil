package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;

public interface ITypeList extends ITypeConsumer
{
	public int typeCount();
	
	public void setType(int index, IType type);
	
	public void addType(IType type);
	
	public IType getType(int index);
	
	@Override
	public default void setType(IType type)
	{
		this.addType(type);
	}
}
