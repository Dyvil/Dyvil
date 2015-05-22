package dyvil.lang.ref.simple;

import dyvil.lang.ref.IObjectRef;

public class ObjectRef<T> implements IObjectRef<T>
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
	public T get()
	{
		return this.value;
	}
	
	@Override
	public void set(T value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value == null ? "null" : this.value.toString();
	}
}
