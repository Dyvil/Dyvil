package dyvil.collections.mutable;

import dyvil.lang.Collection;

public interface MutableCollection<E> extends Collection<E>
{
	@Override
	public default Collection<E> $plus(E entry)
	{
		this.$plus$eq(entry);
		return this;
	}
	
	@Override
	public default Collection<? extends E> $plus(Collection<? extends E> collection)
	{
		this.$plus$eq(collection);
		return this;
	}
	
	@Override
	public default Collection<E> $minus(E entry)
	{
		this.$minus$eq(entry);
		return this;
	}
	
	@Override
	public default Collection<? extends E> $minus(Collection<? extends E> collection)
	{
		this.$minus$eq(collection);
		return this;
	}
	
	@Override
	public default Collection<? extends E> $amp(Collection<? extends E> collection)
	{
		this.$amp$eq(collection);
		return this;
	}
	
	@Override
	public default MutableCollection<E> mutable()
	{
		return this;
	}
}
