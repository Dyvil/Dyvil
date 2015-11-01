package dyvil.reflect;

public class EnumReflection
{
	public static <E extends Enum<E>> E[] getEnumConstants(Class<E> enumClass)
	{
		return ReflectUtils.JAVA_LANG_ACCESS.getEnumConstantsShared(enumClass);
	}
	
	public static <E extends Enum<E>> E getEnumConstant(Class<E> enumClass, int index)
	{
		return getEnumConstants(enumClass)[index];
	}
	
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
	
	public static <E extends Enum<E>> int getEnumCount(Class<E> enumClass)
	{
		return getEnumConstants(enumClass).length;
	}
}
