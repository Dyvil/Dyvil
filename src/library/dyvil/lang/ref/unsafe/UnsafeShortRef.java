package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IShortRef;
import dyvil.reflect.ReflectUtils;

public final class UnsafeShortRef implements IShortRef
{
	private Object	base;
	private long	offset;
	
	public UnsafeShortRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeShortRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeShortRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public short get()
	{
		return ReflectUtils.unsafe.getShort(this.base, this.offset);
	}
	
	@Override
	public void set(short value)
	{
		ReflectUtils.unsafe.putShort(this.base, this.offset, value);
	}
}
