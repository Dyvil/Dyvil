package dyvil.ref.unsafe;

import dyvil.ref.FloatRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeFloatRef implements FloatRef
{
	private final Object base;
	private final long   offset;
	
	public UnsafeFloatRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeFloatRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeFloatRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public float get()
	{
		return ReflectUtils.UNSAFE.getFloat(this.base, this.offset);
	}
	
	@Override
	public void set(float value)
	{
		ReflectUtils.UNSAFE.putFloat(this.base, this.offset, value);
	}
}
