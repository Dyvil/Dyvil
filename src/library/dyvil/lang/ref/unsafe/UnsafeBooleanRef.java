package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.BooleanRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeBooleanRef implements BooleanRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeBooleanRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeBooleanRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeBooleanRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public boolean get()
	{
		return ReflectUtils.UNSAFE.getBoolean(this.base, this.offset);
	}
	
	@Override
	public void set(boolean value)
	{
		ReflectUtils.UNSAFE.putBoolean(this.base, this.offset, value);
	}
}
