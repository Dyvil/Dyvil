package dyvil.arrays;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface CharArray
{
	public static final char[]	EMPTY	= new char[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(char[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CALOAD })
	public static @infix char apply(char[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CASTORE })
	public static @infix void update(char[] array, int i, char v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix char[] $plus(char[] a, char[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		char[] res = new char[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
}
