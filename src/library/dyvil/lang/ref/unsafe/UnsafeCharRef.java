package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.CharRef;

import dyvil.reflect.ReflectUtils;

public final class UnsafeCharRef implements CharRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeCharRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeCharRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeCharRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public char get()
	{
		return ReflectUtils.unsafe.getChar(this.base, this.offset);
	}
	
	@Override
	public void set(char value)
	{
		ReflectUtils.unsafe.putChar(this.base, this.offset, value);
	}
}
