package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.DoubleRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeDoubleRef implements DoubleRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeDoubleRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeDoubleRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeDoubleRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public double get()
	{
		return ReflectUtils.UNSAFE.getDouble(this.base, this.offset);
	}
	
	@Override
	public void set(double value)
	{
		ReflectUtils.UNSAFE.putDouble(this.base, this.offset, value);
	}
}
