package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;

public interface ITyped extends ITypeConsumer
{
	@Override
	public void setType(IType type);
	
	public IType getType();
	
	public default boolean isType(IType type)
	{
		return this.getType() == type;
	}
}
