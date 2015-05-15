package dyvil.lang.ref;

public interface ObjectRef$<T>
{
	public T apply();
	
	public void update(T value);
}
