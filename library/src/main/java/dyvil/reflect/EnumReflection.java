package dyvil.reflect;

/**
 * @deprecated since v0.47.0
 */
@Deprecated
public class EnumReflection
{
	/**
	 * @deprecated since v0.47.0; use {@code enumClass}.{@link Class#getEnumConstants() getEnumConstants()} instead
	 */
	@Deprecated
	public static <E extends Enum<E>> E[] getEnumConstants(Class<E> enumClass)
	{
		return enumClass.getEnumConstants();
	}

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static <E extends Enum<E>> E getEnumConstant(Class<E> enumClass, int index)
	{
		return getEnumConstants(enumClass)[index];
	}

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static <E extends Enum<E>> E getEnumConstant(Class<E> enumClass, String name)
	{
		for (E e : getEnumConstants(enumClass))
		{
			if (e.name().equalsIgnoreCase(name))
			{
				return e;
			}
		}
		return null;
	}

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static <E extends Enum<E>> int getEnumCount(Class<E> enumClass)
	{
		return getEnumConstants(enumClass).length;
	}
}
