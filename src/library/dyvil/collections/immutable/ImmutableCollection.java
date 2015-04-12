package dyvil.collections.immutable;

import dyvil.lang.Collection;
import dyvil.lang.ImmutableException;

public interface ImmutableCollection<E> extends Collection<E>
{
	@Override
	public default void $plus$eq(E entry)
	{
		throw new ImmutableException("+= on Immutable Collection");
	}
	
	@Override
	public default void $plus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("+= on Immutable Collection");
	}
	
	@Override
	public default void $minus$eq(E entry)
	{
		throw new ImmutableException("-= on Immutable Collection");
	}
	
	@Override
	public default void $minus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("-= on Immutable Collection");
	}
	
	@Override
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable Collection");
	}
	
	@Override
	public default ImmutableCollection<E> immutable()
	{
		return this;
	}
}
