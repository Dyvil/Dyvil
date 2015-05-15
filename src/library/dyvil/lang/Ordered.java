package dyvil.lang;

public interface Ordered<T extends Ordered<T>> extends Comparable<T>
{
	public boolean $eq$eq(T t);
	
	public default boolean $bang$eq(T t)
	{
		return !$eq$eq(t);
	}
	
	public boolean $lt(T t);
	
	public default boolean $lt$eq(T t)
	{
		return $lt(t) || $eq$eq(t);
	}
	
	public default boolean $gt(T t)
	{
		return !$lt$eq(t);
	}
	
	public default boolean $gt$eq(T t)
	{
		return !$lt(t);
	}
	
	@Override
	public default int compareTo(T o)
	{
		return this.$lt(o) ? -1 : this.$eq$eq(o) ? 0 : 1;
	}
	
	public default T next()
	{
		throw new IllegalArgumentException("next() not implemented!");
	}
	
	public default T previous()
	{
		throw new IllegalArgumentException("previous() not implemented");
	}
	
	public default int distanceTo(T o)
	{
		return -1;
	}
}
