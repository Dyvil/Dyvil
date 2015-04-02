package dyvil.lang;

public interface Ordered<T>
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
}
