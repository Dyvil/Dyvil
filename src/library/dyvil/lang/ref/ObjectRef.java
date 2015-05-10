package dyvil.lang.ref;

public class ObjectRef<T> implements ObjectRef$<T>
{
	public T	value;
	
	public static <T> ObjectRef<T> apply(T value)
	{
		return new ObjectRef(value);
	}
	
	public ObjectRef(T value)
	{
		this.value = value;
	}
	
	@Override
	public T apply()
	{
		return this.value;
	}
	
	@Override
	public void update(T value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value == null ? "null" : this.value.toString();
	}
}
