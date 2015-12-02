package dyvil.lang.ref.unsafe;

import dyvil.lang.ref.ObjectRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeObjectRef<T> implements ObjectRef<T>
{
	private final Object base;
	private final long   offset;
	
	public UnsafeObjectRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeObjectRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeObjectRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public T get()
	{
		return (T) ReflectUtils.UNSAFE.getObject(this.base, this.offset);
	}
	
	@Override
	public void set(T value)
	{
		ReflectUtils.UNSAFE.putObject(this.base, this.offset, value);
	}
}
