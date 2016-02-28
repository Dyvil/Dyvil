package dyvil.ref.unsafe;

import dyvil.ref.ShortRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeShortRef implements ShortRef
{
	private final Object base;
	private final long   offset;
	
	public UnsafeShortRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeShortRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeShortRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public short get()
	{
		return ReflectUtils.UNSAFE.getShort(this.base, this.offset);
	}
	
	@Override
	public void set(short value)
	{
		ReflectUtils.UNSAFE.putShort(this.base, this.offset, value);
	}
}
