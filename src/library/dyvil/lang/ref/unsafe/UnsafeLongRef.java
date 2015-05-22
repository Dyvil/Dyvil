package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.ILongRef;
import dyvil.reflect.ReflectUtils;

public final class UnsafeLongRef implements ILongRef
{
	private Object	base;
	private long	offset;
	
	public UnsafeLongRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeLongRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeLongRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public long get()
	{
		return ReflectUtils.unsafe.getLong(this.base, this.offset);
	}
	
	@Override
	public void set(long value)
	{
		ReflectUtils.unsafe.putLong(this.base, this.offset, value);
	}
}
