package dyvil.lang.ref.unsafe;

import java.lang.reflect.Field;

import dyvil.lang.ref.IByteRef;
import dyvil.reflect.ReflectUtils;

public final class UnsafeByteRef implements IByteRef
{
	private final Object	base;
	private final long		offset;
	
	public UnsafeByteRef(Field staticField)
	{
		this.base = ReflectUtils.unsafe.staticFieldBase(staticField);
		this.offset = ReflectUtils.unsafe.staticFieldOffset(staticField);
	}
	
	public UnsafeByteRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.unsafe.objectFieldOffset(field);
	}
	
	public UnsafeByteRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public byte get()
	{
		return ReflectUtils.unsafe.getByte(this.base, this.offset);
	}
	
	@Override
	public void set(byte value)
	{
		ReflectUtils.unsafe.putByte(this.base, this.offset, value);
	}
}
