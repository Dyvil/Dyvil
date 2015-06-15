package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IStringRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeStringRef implements IStringRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeStringRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeStringRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeStringRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public String get()
	{
		return (String) ReflectUtils.unsafe.getObject(this.base, this.offset);
	}
	
	@Override
	public void set(String value)
	{
		ReflectUtils.unsafe.putObject(this.base, this.offset, value);
	}
}
