package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IDoubleRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeDoubleRef implements IDoubleRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeDoubleRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeDoubleRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeDoubleRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public double get()
	{
		return ReflectUtils.unsafe.getDouble(this.base, this.offset);
	}
	
	@Override
	public void set(double value)
	{
		ReflectUtils.unsafe.putDouble(this.base, this.offset, value);
	}
}
