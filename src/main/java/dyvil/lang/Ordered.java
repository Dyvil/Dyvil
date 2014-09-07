package dyvil.lang;

public interface Ordered<T>
{
	public boolean $eq$eq(T t);
	
	public default boolean $bang$eq(T t)
	{
		return !$eq$eq(t);
	}
	
	public boolean $less(T t);
	
	public default boolean $less$eq(T t)
	{
		return $less(t) || $eq$eq(t);
	}
	
	public default boolean $greater(T t)
	{
		return !$less$eq(t);
	}
	
	public default boolean $greater$eq(T t)
	{
		return !$less(t);
	}
}
