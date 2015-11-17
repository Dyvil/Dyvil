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
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeCharRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeCharRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public char get()
	{
		return ReflectUtils.UNSAFE.getChar(this.base, this.offset);
	}
	
	@Override
	public void set(char value)
	{
		ReflectUtils.UNSAFE.putChar(this.base, this.offset, value);
	}
}
