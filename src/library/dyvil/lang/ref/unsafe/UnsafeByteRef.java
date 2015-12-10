package dyvil.lang.ref.unsafe;

import dyvil.lang.ref.ByteRef;
import dyvil.reflect.ReflectUtils;

import java.lang.reflect.Field;

public final class UnsafeByteRef implements ByteRef
{
	private final Object base;
	private final long   offset;
	
	public UnsafeByteRef(Field staticField)
	{
		this.base = ReflectUtils.UNSAFE.staticFieldBase(staticField);
		this.offset = ReflectUtils.UNSAFE.staticFieldOffset(staticField);
	}
	
	public UnsafeByteRef(Object instance, Field field)
	{
		this.base = instance;
		this.offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
	}
	
	public UnsafeByteRef(Object base, long offset)
	{
		this.base = base;
		this.offset = offset;
	}
	
	@Override
	public byte get()
	{
		return ReflectUtils.UNSAFE.getByte(this.base, this.offset);
	}
	
	@Override
	public void set(byte value)
	{
		ReflectUtils.UNSAFE.putByte(this.base, this.offset, value);
	}
}
