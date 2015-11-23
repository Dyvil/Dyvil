package dyvil.runtime;

import dyvil.lang.ref.BooleanRef;
import dyvil.lang.ref.IntRef;
import dyvil.lang.ref.unsafe.UnsafeBooleanRef;
import dyvil.lang.ref.unsafe.UnsafeIntRef;

import java.lang.reflect.Field;

public final class FieldAccessFactory
{
	private static Field getField(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return type.getDeclaredField(fieldName);
	}

	public static BooleanRef newBooleanRef(Object base, Class<?> type, String fieldName)
			throws NoSuchFieldException
	{
		return new UnsafeBooleanRef(base, getField(type, fieldName));
	}

	public static IntRef newStaticIntRef(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(getField(type, fieldName));
	}

	public static IntRef newIntRef(Object base, Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return new UnsafeIntRef(base, getField(type, fieldName));
	}

	// TODO Add the other methods
}
