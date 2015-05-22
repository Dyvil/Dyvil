package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IFloatRef;
import dyvil.reflect.ReflectUtils;

public final class UnsafeFloatRef implements IFloatRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeFloatRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeFloatRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeFloatRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public float get()
	{
		return ReflectUtils.unsafe.getFloat(this.base, this.offset);
	}
	
	@Override
	public void set(float value)
	{
		ReflectUtils.unsafe.putFloat(this.base, this.offset, value);
	}
}
