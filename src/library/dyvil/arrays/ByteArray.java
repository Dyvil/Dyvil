package dyvil.arrays;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface ByteArray
{
	public static final byte[]	EMPTY	= new byte[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(byte[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BALOAD })
	public static @infix byte apply(byte[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BASTORE })
	public static @infix void update(byte[] array, int i, byte v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix byte[] $plus(byte[] a, byte[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		byte[] res = new byte[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
}
