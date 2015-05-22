package dyvil.lang.ref;

public interface IObjectRef<T>
{
	public T get();
	
	public void set(T value);
}
