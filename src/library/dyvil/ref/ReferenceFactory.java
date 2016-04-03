package dyvil.ref;

import dyvil.ref.array.*;
import dyvil.ref.unsafe.*;
import dyvil.runtime.reference.PropertyReferenceMetafactory;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public final class ReferenceFactory
{
	private static Field getField(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return type.getDeclaredField(fieldName);
	}

	// Property Factory

	public static CallSite propertyRefMetafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, MethodHandle getter, MethodHandle setter)
		throws Exception
	{
		final PropertyReferenceMetafactory prm = new PropertyReferenceMetafactory(caller, invokedType,
		                                                                          getter, setter);
		return prm.buildCallSite();
	}

	// Boolean

	public static BooleanRef newBooleanRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeBooleanRef(base, getField(type, fieldName));
	}

	public static BooleanRef newStaticBooleanRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeBooleanRef(getField(type, fieldName));
	}

	public static BooleanRef newBooleanArrayRef(boolean[] array, int index)
	{
		return new BooleanArrayRef(array, index);
	}

	// Byte

	public static ByteRef newByteRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeByteRef(base, getField(type, fieldName));
	}

	public static ByteRef newStaticByteRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeByteRef(getField(type, fieldName));
	}

	public static ByteRef newByteArrayRef(byte[] array, int index)
	{
		return new ByteArrayRef(array, index);
	}

	// Short

	public static ShortRef newShortRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeShortRef(base, getField(type, fieldName));
	}

	public static ShortRef newStaticShortRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeShortRef(getField(type, fieldName));
	}

	public static ShortRef newShortArrayRef(short[] array, int index)
	{
		return new ShortArrayRef(array, index);
	}

	// Char

	public static CharRef newCharRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeCharRef(base, getField(type, fieldName));
	}

	public static CharRef newStaticCharRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeCharRef(getField(type, fieldName));
	}

	public static CharRef newCharArrayRef(char[] array, int index)
	{
		return new CharArrayRef(array, index);
	}

	// Int

	public static IntRef newIntRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(base, getField(type, fieldName));
	}

	public static IntRef newStaticIntRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(getField(type, fieldName));
	}

	public static IntRef newIntArrayRef(int[] array, int index)
	{
		return new IntArrayRef(array, index);
	}

	// Long

	public static LongRef newLongRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeLongRef(base, getField(type, fieldName));
	}

	public static LongRef newStaticLongRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeLongRef(getField(type, fieldName));
	}

	public static LongRef newLongArrayRef(long[] array, int index)
	{
		return new LongArrayRef(array, index);
	}

	// Float

	public static FloatRef newFloatRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeFloatRef(base, getField(type, fieldName));
	}

	public static FloatRef newStaticFloatRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeFloatRef(getField(type, fieldName));
	}

	public static FloatRef newFloatArrayRef(float[] array, int index)
	{
		return new FloatArrayRef(array, index);
	}

	// Double

	public static DoubleRef newDoubleRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeDoubleRef(base, getField(type, fieldName));
	}

	public static DoubleRef newStaticDoubleRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeDoubleRef(getField(type, fieldName));
	}

	public static DoubleRef newDoubleArrayRef(double[] array, int index)
	{
		return new DoubleArrayRef(array, index);
	}

	// Object

	public static ObjectRef newObjectRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeObjectRef(base, getField(type, fieldName));
	}

	public static ObjectRef newStaticObjectRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeObjectRef(getField(type, fieldName));
	}

	public static ObjectRef newObjectArrayRef(Object[] array, int index)
	{
		return new ObjectArrayRef<>(array, index);
	}
}
