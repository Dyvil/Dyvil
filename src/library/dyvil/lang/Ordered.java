package dyvil.lang;

public interface Ordered<T extends Ordered<T>> extends Comparable<T>
{
	public default boolean $eq$eq(T t)
	{
		return this.compareTo(t) == 0;
	}
	
	public default boolean $bang$eq(T t)
	{
		return this.compareTo(t) != 0;
	}
	
	public default boolean $lt(T t)
	{
		return this.compareTo(t) < 0;
	}
	
	public default boolean $lt$eq(T t)
	{
		return this.compareTo(t) <= 0;
	}
	
	public default boolean $gt(T t)
	{
		return this.compareTo(t) > 0;
	}
	
	public default boolean $gt$eq(T t)
	{
		return this.compareTo(t) >= 0;
	}
	
	@Override
	public int compareTo(T o);
}
