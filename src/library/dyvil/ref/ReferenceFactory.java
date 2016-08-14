package dyvil.ref;

import dyvil.ref.array.*;
import dyvil.ref.unsafe.*;
import dyvil.reflect.ReflectUtils;
import dyvil.runtime.reference.PropertyReferenceMetafactory;

import java.lang.invoke.*;
import java.lang.reflect.Field;

public final class ReferenceFactory
{
	private static final MethodType STATIC_CONSTRUCTOR_TYPE   = MethodType.methodType(void.class, Field.class);
	private static final MethodType INSTANCE_CONSTRUCTOR_TYPE = MethodType
		                                                            .methodType(void.class, Object.class, long.class);

	private static Field getField(Class<?> type, String fieldName) throws NoSuchFieldException
	{
		return type.getDeclaredField(fieldName);
	}

	public static long getObjectFieldOffset(Class<?> type, String fieldName)
	{
		try
		{
			return ReflectUtils.UNSAFE.objectFieldOffset(type.getDeclaredField(fieldName));
		}
		catch (NoSuchFieldException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	// Bootstrap Factories

	public static CallSite instanceRefMetafactory(MethodHandles.Lookup caller, String fieldName, MethodType methodType)
		throws Throwable
	{
		final Class<?> enclosingClass = methodType.parameterType(0);
		final Class<?> targetType = methodType.returnType();
		// The field to reference and its offset
		final Field field = getField(enclosingClass, fieldName);
		final long offset = ReflectUtils.UNSAFE.objectFieldOffset(field);
		// The Unsafe*Ref constructor
		final MethodHandle constructor = caller.findConstructor(targetType, INSTANCE_CONSTRUCTOR_TYPE);
		// Insert the offset argument, returning a method of type (enclosingClass)Unsafe*Ref
		return new ConstantCallSite(MethodHandles.insertArguments(constructor, 1, offset).asType(methodType));
	}

	public static CallSite staticRefMetafactory(MethodHandles.Lookup caller, String fieldName, MethodType methodType,
		                                           Class<?> enclosingClass) throws Throwable
	{
		final Class<?> targetType = methodType.returnType();
		// The field to reference
		final Field field = getField(enclosingClass, fieldName);
		// The Unsafe*Ref(Field) constructor
		final MethodHandle constructor = caller.findConstructor(targetType, STATIC_CONSTRUCTOR_TYPE);
		// The resulting Unsafe*Ref
		final Object result = constructor.invoke(field);
		return new ConstantCallSite(MethodHandles.constant(targetType, result));
	}

	public static CallSite propertyRefMetafactory(MethodHandles.Lookup caller,
		                                             @SuppressWarnings("UnusedParameters") String invokedName,
		                                             MethodType invokedType, MethodHandle getter, MethodHandle setter)
		throws Exception
	{
		final PropertyReferenceMetafactory prm = new PropertyReferenceMetafactory(caller, invokedType, getter, setter);
		return prm.buildCallSite();
	}

	// Boolean

	public static BooleanRef newBooleanRef(Object base, long offset)
	{
		return new UnsafeBooleanRef(base, offset);
	}

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

	public static ByteRef newByteRef(Object base, long offset)
	{
		return new UnsafeByteRef(base, offset);
	}

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

	public static ShortRef newShortRef(Object base, long offset)
	{
		return new UnsafeShortRef(base, offset);
	}

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

	public static CharRef newCharRef(Object base, long offset)
	{
		return new UnsafeCharRef(base, offset);
	}

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

	public static IntRef newIntRef(Object base, long offset)
	{
		return new UnsafeIntRef(base, offset);
	}

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

	public static LongRef newLongRef(Object base, long offset)
	{
		return new UnsafeLongRef(base, offset);
	}

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

	public static FloatRef newFloatRef(Object base, long offset)
	{
		return new UnsafeFloatRef(base, offset);
	}

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

	public static DoubleRef newDoubleRef(Object base, long offset)
	{
		return new UnsafeDoubleRef(base, offset);
	}

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

	public static ObjectRef newObjectRef(Object base, long offset)
	{
		return new UnsafeObjectRef(base, offset);
	}

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
