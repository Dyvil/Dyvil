package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IntRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeIntRef implements IntRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeIntRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeIntRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeIntRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public int get()
	{
		return ReflectUtils.unsafe.getInt(this.base, this.offset);
	}
	
	@Override
	public void set(int value)
	{
		ReflectUtils.unsafe.putInt(this.base, this.offset, value);
	}
}
