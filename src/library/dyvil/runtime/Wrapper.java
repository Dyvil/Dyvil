package dyvil.runtime;

import dyvil.lang.*;
import dyvil.lang.Boolean;
import dyvil.lang.Byte;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.lang.Short;
import dyvil.lang.Void;

public enum Wrapper
{
	BOOLEAN('Z', boolean.class, Boolean.class, 1),
	BYTE('B', byte.class, Byte.class, 1),
	SHORT('S', short.class, Short.class, 2),
	CHAR('C', char.class, Char.class, 2 | 1 << 4),
	INT('I', int.class, Int.class, 4),
	LONG('J', long.class, Long.class, 8),
	FLOAT('F', float.class, Float.class, 4 | 1 << 5),
	DOUBLE('D', double.class, Double.class, 8 | 1 << 5),
	OBJECT('L', Object.class, Object.class, 8 | 1 << 6),
	VOID('V', void.class, Void.class, 1 << 6);
	
	private static final int		SIZE_MASK		= (1 << 4) - 1;
	private static final int		UNSIGNED		= 1 << 4;
	private static final int		FLOATING		= 1 << 5;
	private static final int		OTHER			= 1 << 6;
	
	private char					basicTypeChar;
	private Class					primitiveClass;
	private Class					wrapperClass;
	private String					primitiveSimpleName;
	private String					wrapperSimpleName;
	
	// 0 OTHER FLOATING SIGNED SIZE3 SIZE2 SIZE1 SIZE0
	private byte					flags;
	
	private static final Wrapper[]	FROM_PRIMITIVE	= new Wrapper[16];
	private static final Wrapper[]	FROM_WRAPPER	= new Wrapper[16];
	private static final Wrapper[]	FROM_CHAR		= new Wrapper[16];
	
	static
	{
		
		for (Wrapper localWrapper : values())
		{
			int k = hashPrimitive(localWrapper.primitiveClass);
			int l = hashWrapper(localWrapper.wrapperClass);
			int i1 = hashChar(localWrapper.basicTypeChar);
			assert FROM_PRIMITIVE[k] == null;
			assert FROM_WRAPPER[l] == null;
			assert FROM_CHAR[i1] == null;
			FROM_PRIMITIVE[k] = localWrapper;
			FROM_WRAPPER[l] = localWrapper;
			FROM_CHAR[i1] = localWrapper;
		}
	}
	
	Wrapper(char basicTypeChar, Class primitive, Class wrapper, int flags)
	{
		this.basicTypeChar = basicTypeChar;
		this.primitiveClass = primitive;
		this.wrapperClass = wrapper;
		this.primitiveSimpleName = primitive.getSimpleName();
		this.wrapperSimpleName = wrapper.getSimpleName();
		this.flags = (byte) flags;
	}
	
	public static Wrapper forBasicType(char c)
	{
		return FROM_CHAR[hashChar(c)];
	}
	
	public static Wrapper forPrimitiveType(Class c)
	{
		return FROM_PRIMITIVE[hashPrimitive(c)];
	}
	
	public static Wrapper forWrapperType(Class c)
	{
		return FROM_WRAPPER[hashWrapper(c)];
	}
	
	public static boolean isWrapperType(Class c)
	{
		Wrapper w = FROM_WRAPPER[hashWrapper(c)];
		return w != null && w.wrapperClass == c;
	}
	
	private static int hashPrimitive(Class<?> paramClass)
	{
		String str = paramClass.getName();
		if (str.length() < 3)
		{
			return 0;
		}
		return (str.charAt(0) + str.charAt(2)) % 16;
	}
	
	private static int hashWrapper(Class<?> paramClass)
	{
		String str = paramClass.getName();
		if (str.length() < 13)
		{
			return 0;
		}
		return 3 * str.charAt(12) + str.charAt(13) & 0xF;
	}
	
	private static int hashChar(char paramChar)
	{
		return (paramChar + (paramChar >> 1)) & 15;
	}
	
	public char basicTypeChar()
	{
		return this.basicTypeChar;
	}
	
	public Class primitiveType()
	{
		return this.primitiveClass;
	}
	
	public Class wrapperType()
	{
		return this.wrapperClass;
	}
	
	public String primitiveSimpleName()
	{
		return this.primitiveSimpleName;
	}
	
	public String wrapperSimpleName()
	{
		return this.wrapperSimpleName;
	}
	
	public boolean isConvertibleFrom(Wrapper wrapper)
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
