package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;

public interface ITyped extends ITypeConsumer
{
	@Override
	void setType(IType type);
	
	IType getType();
	
	default boolean isType(IType type)
	{
		return this.getType() == type;
	}
}
