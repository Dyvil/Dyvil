package dyvil.ref.unsafe;

import dyvil.ref.StringRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeStringRef implements StringRef
{
	private final Object base;
	private final long   offset;
	
	public UnsafeStringRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeStringRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeStringRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public String get()
	{
		return (String) ReflectUtils.UNSAFE.getObject(this.base, this.offset);
	}
	
	@Override
	public void set(String value)
	{
		ReflectUtils.UNSAFE.putObject(this.base, this.offset, value);
	}
}
