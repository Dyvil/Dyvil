package dyvil.lang.ref;

public interface ObjectRef<T>
{
	public T get();
	
	public void set(T value);
}
