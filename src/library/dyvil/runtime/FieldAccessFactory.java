package dyvil.runtime;

import dyvil.lang.ref.*;
import dyvil.lang.ref.unsafe.*;

import java.lang.reflect.Field;

public final class FieldAccessFactory
{
	private static Field getField(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return type.getDeclaredField(fieldName);
	}

	// Boolean

	public static BooleanRef newStaticBooleanRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeBooleanRef(getField(type, fieldName));
	}

	public static BooleanRef newBooleanRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeBooleanRef(base, getField(type, fieldName));
	}

	// Byte

	public static ByteRef newStaticByteRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeByteRef(getField(type, fieldName));
	}

	public static ByteRef newByteRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeByteRef(base, getField(type, fieldName));
	}

	// Short

	public static ShortRef newStaticShortRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeShortRef(getField(type, fieldName));
	}

	public static ShortRef newShortRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeShortRef(base, getField(type, fieldName));
	}

	// Char

	public static CharRef newStaticCharRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeCharRef(getField(type, fieldName));
	}

	public static CharRef newCharRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeCharRef(base, getField(type, fieldName));
	}

	// Int

	public static IntRef newStaticIntRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(getField(type, fieldName));
	}

	public static IntRef newIntRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(base, getField(type, fieldName));
	}

	// Long

	public static LongRef newStaticLongRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeLongRef(getField(type, fieldName));
	}

	public static LongRef newLongRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeLongRef(base, getField(type, fieldName));
	}

	// Float

	public static FloatRef newStaticFloatRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeFloatRef(getField(type, fieldName));
	}

	public static FloatRef newFloatRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeFloatRef(base, getField(type, fieldName));
	}

	// Double

	public static DoubleRef newStaticDoubleRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeDoubleRef(getField(type, fieldName));
	}

	public static DoubleRef newDoubleRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeDoubleRef(base, getField(type, fieldName));
	}

	// Object

	public static ObjectRef newStaticObjectRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeObjectRef(getField(type, fieldName));
	}

	public static ObjectRef newObjectRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeObjectRef(base, getField(type, fieldName));
	}
}
