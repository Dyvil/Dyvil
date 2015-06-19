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
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeBooleanRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeBooleanRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public boolean get()
	{
		return ReflectUtils.unsafe.getBoolean(this.base, this.offset);
	}
	
	@Override
	public void set(boolean value)
	{
		ReflectUtils.unsafe.putBoolean(this.base, this.offset, value);
	}
}
