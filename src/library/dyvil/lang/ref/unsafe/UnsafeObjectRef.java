package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IObjectRef;
import dyvil.reflect.ReflectUtils;

public final class UnsafeObjectRef<T> implements IObjectRef<T>
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeObjectRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeObjectRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeObjectRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public T get()
	{
		return (T) ReflectUtils.unsafe.getObject(this.base, this.offset);
	}
	
	@Override
	public void set(T value)
	{
		ReflectUtils.unsafe.putObject(this.base, this.offset, value);
	}
}
