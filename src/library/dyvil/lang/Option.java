package dyvil.lang;

public interface Option<T>
{
	public static <T> Option<T> apply(T t)
	{
		return new Some(t);
	}
	
	public static Option none()
	{
		return None.instance;
	}
	
	public T get();
	
	public boolean isEmpty();
	
	public default boolean isDefined()
	{
		return !isEmpty();
	}
}
