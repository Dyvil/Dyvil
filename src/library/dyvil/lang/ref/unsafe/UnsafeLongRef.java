package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.LongRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeLongRef implements LongRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeLongRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeLongRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeLongRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public long get()
	{
		return ReflectUtils.UNSAFE.getLong(this.base, this.offset);
	}
	
	@Override
	public void set(long value)
	{
		ReflectUtils.UNSAFE.putLong(this.base, this.offset, value);
	}
}
