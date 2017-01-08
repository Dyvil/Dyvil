package dyvil.runtime;

import dyvil.annotation.internal.NonNull;

public enum Wrapper
{
	BOOLEAN('Z', boolean.class, Boolean.class, 1),
	BYTE('B', byte.class, Byte.class, 1),
	SHORT('S', short.class, Short.class, 2),
	CHAR('C', char.class, Character.class, 2 | 1 << 4),
	INT('I', int.class, Integer.class, 4),
	LONG('J', long.class, Long.class, 8),
	FLOAT('F', float.class, Float.class, 4 | 1 << 5),
	DOUBLE('D', double.class, Double.class, 8 | 1 << 5),
	OBJECT('L', Object.class, Object.class, 8 | 1 << 6),
	VOID('V', void.class, Void.class, 1 << 6);

	private static final int SIZE_MASK = (1 << 4) - 1;
	private static final int UNSIGNED  = 1 << 4;
	private static final int FLOATING  = 1 << 5;
	private static final int OTHER     = 1 << 6;

	private final char   basicTypeChar;
	@NonNull
	private final Class  primitiveClass;
	@NonNull
	private final Class  wrapperClass;
	@NonNull
	private final String primitiveSimpleName;
	@NonNull
	private final String wrapperSimpleName;

	// 0 OTHER FLOATING SIGNED SIZE3 SIZE2 SIZE1 SIZE0
	private final byte flags;

	private static final Wrapper[] FROM_PRIMITIVE = new Wrapper[16];
	private static final Wrapper[] FROM_WRAPPER   = new Wrapper[16];
	private static final Wrapper[] FROM_CHAR      = new Wrapper[16];

	static
	{
		for (Wrapper localWrapper : values())
		{
			int primitiveHash = hashPrimitive(localWrapper.primitiveClass);
			int wrapperHash = hashWrapper(localWrapper.wrapperClass);
			int charHash = hashChar(localWrapper.basicTypeChar);
			assert FROM_PRIMITIVE[primitiveHash] == null;
			assert FROM_WRAPPER[wrapperHash] == null;
			assert FROM_CHAR[charHash] == null;
			FROM_PRIMITIVE[primitiveHash] = localWrapper;
			FROM_WRAPPER[wrapperHash] = localWrapper;
			FROM_CHAR[charHash] = localWrapper;
		}
	}

	Wrapper(char basicTypeChar, @NonNull Class<?> primitive, @NonNull Class<?> wrapper, int flags)
	{
		this.basicTypeChar = basicTypeChar;
		this.primitiveClass = primitive;
		this.wrapperClass = wrapper;
		this.primitiveSimpleName = primitive.getSimpleName();
		this.wrapperSimpleName = wrapper.getSimpleName();
		this.flags = (byte) flags;
	}

	@NonNull
	public static Class<?> referenceType(@NonNull Class<?> forClass)
	{
		if (forClass.isPrimitive())
		{
			return forPrimitiveType(forClass).wrapperClass;
		}
		return forClass;
	}

	public static Wrapper forBasicType(char c)
	{
		return FROM_CHAR[hashChar(c)];
	}

	public static Wrapper forPrimitiveType(@NonNull Class<?> c)
	{
		return FROM_PRIMITIVE[hashPrimitive(c)];
	}

	public static Wrapper forWrapperType(@NonNull Class<?> c)
	{
		return FROM_WRAPPER[hashWrapper(c)];
	}

	public static boolean isWrapperType(@NonNull Class<?> wrapperClass)
	{
		final Wrapper wrapper = FROM_WRAPPER[hashWrapper(wrapperClass)];
		return wrapper != null && wrapper.wrapperClass == wrapperClass;
	}

	private static int hashPrimitive(@NonNull Class<?> primitiveClass)
	{
		final String className = primitiveClass.getName();

		if (className.length() < 3)
		{
			return 0;
		}
		return (className.charAt(0) + className.charAt(2)) & 0xF;
	}

	private static int hashWrapper(@NonNull Class<?> wrapperClass)
	{
		final String className = wrapperClass.getName();
		if (className.length() < 13)
		{
			return 0;
		}
		return (3 * className.charAt(11) + className.charAt(12)) & 0xF;
	}

	private static int hashChar(char typeChar)
	{
		return (typeChar + (typeChar >> 1)) & 15;
	}

	public char basicTypeChar()
	{
		return this.basicTypeChar;
	}

	@NonNull
	public Class primitiveType()
	{
		return this.primitiveClass;
	}

	@NonNull
	public Class wrapperType()
	{
		return this.wrapperClass;
	}

	@NonNull
	public String primitiveSimpleName()
	{
		return this.primitiveSimpleName;
	}

	@NonNull
	public String wrapperSimpleName()
	{
		return this.wrapperSimpleName;
	}

	public boolean isConvertibleFrom(@NonNull Wrapper wrapper)
	{
		if (this == wrapper)
		{
			return true;
		}
		if (this.compareTo(wrapper) < 0)
		{
			return false;
		}
		// All conversions are allowed in the enum order between floats and
		// signed ints.
		// First detect non-signed non-float types (boolean, char, Object,
		// void).
		if ((this.flags & wrapper.flags & UNSIGNED) != 0)
		{
			if ((this.flags & OTHER) != 0)
			{
				return true;
			}
			// can convert char to int or wider, but nothing else
			// no other conversions are classified as widening
			return wrapper == CHAR;
		}

		return true;
	}

	public boolean isSigned()
	{
		return (this.flags & UNSIGNED) == 0;
	}

	public boolean isFloating()
	{
		return (this.flags & FLOATING) != 0;
	}
}
