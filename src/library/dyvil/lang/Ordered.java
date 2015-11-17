package dyvil.lang;

public interface Ordered<T extends Ordered<T>> extends Comparable<T>
{
	default boolean $eq$eq(T t)
	{
		return this.compareTo(t) == 0;
	}
	
	default boolean $bang$eq(T t)
	{
		return this.compareTo(t) != 0;
	}
	
	default boolean $lt(T t)
	{
		return this.compareTo(t) < 0;
	}
	
	default boolean $lt$eq(T t)
	{
		return this.compareTo(t) <= 0;
	}
	
	default boolean $gt(T t)
	{
		return this.compareTo(t) > 0;
	}
	
	default boolean $gt$eq(T t)
	{
		return this.compareTo(t) >= 0;
	}
	
	@Override
	int compareTo(T o);
}
