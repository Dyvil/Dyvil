package dyvil.lang.ref.unsafe;

import dyvil.lang.ref.IntRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeIntRef implements IntRef
{
	private final Object base;
	private final long   offset;
	
	public UnsafeIntRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeIntRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeIntRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public int get()
	{
		return ReflectUtils.UNSAFE.getInt(this.base, this.offset);
	}
	
	@Override
	public void set(int value)
	{
		ReflectUtils.UNSAFE.putInt(this.base, this.offset, value);
	}
}
