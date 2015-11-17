package dyvil.lang.ref;

public interface ObjectRef<T>
{
	T get();
	
	void set(T value);
}
