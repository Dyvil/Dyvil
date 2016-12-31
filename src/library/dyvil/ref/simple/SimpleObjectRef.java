package dyvil.ref.simple;

import dyvil.annotation.internal.NonNull;
import dyvil.ref.ObjectRef;

public class SimpleObjectRef<T> implements ObjectRef<T>
{
	public T value;

	@NonNull
	public static <T> SimpleObjectRef<T> apply(T value)
	{
		return new SimpleObjectRef<>(value);
	}

	public SimpleObjectRef(T value)
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
}
