package dyvil.arrays;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface ShortArray
{
	public static final short[]	EMPTY	= new short[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(short[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, SALOAD })
	public static @infix short apply(short[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, SASTORE })
	public static @infix void update(short[] array, int i, short v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix short[] $plus(short[] a, short[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		short[] res = new short[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
}
