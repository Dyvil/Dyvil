package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.sealed;

public @sealed interface Array
{
	// Array Length
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix <T> int length(T[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(byte[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(short[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(char[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(int[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(long[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(float[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(double[] array)
	{
		return array.length;
	}
	
	// Apply (Array Load)
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AALOAD })
	public static @infix <T> T apply(T[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BALOAD })
	public static @infix byte apply(byte[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, SALOAD })
	public static @infix short apply(short[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CALOAD })
	public static @infix char apply(char[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IALOAD })
	public static @infix int apply(int[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LALOAD })
	public static @infix long apply(long[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FALOAD })
	public static @infix float apply(float[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DALOAD })
	public static @infix double apply(double[] array, int i)
	{
		return array[i];
	}
	
	// Update (Array Store)
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AASTORE })
	public static @infix <T> void update(T[] array, int i, T v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BASTORE })
	public static @infix void update(byte[] array, int i, byte v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, SASTORE })
	public static @infix void update(short[] array, int i, short v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CASTORE })
	public static @infix void update(char[] array, int i, char v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IASTORE })
	public static @infix void update(int[] array, int i, int v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LASTORE })
	public static @infix void update(long[] array, int i, long v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FASTORE })
	public static @infix void update(float[] array, int i, float v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DASTORE })
	public static @infix void update(double[] array, int i, double v)
	{
		array[i] = v;
	}
}
