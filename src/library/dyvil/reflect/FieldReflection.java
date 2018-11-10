package dyvil.reflect;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldReflection
{
	private static final @Nullable Field modifiersField;

	static
	{
		Field modField;

		try
		{
			modField = Field.class.getDeclaredField("modifiers");
			// Makes the 'modifiers' field of the java.lang.reflect.Field class
			// accessible
			modField.setAccessible(true);
		}
		catch (ReflectiveOperationException ignored)
		{
			modField = null;
		}

		modifiersField = modField;
	}

	/**
	 * Adds the modifiers {@code mod} to the given {@link Field} {@code field} if {@code flag} is true, and removed them
	 * otherwise.
	 *
	 * @param field
	 * 	the field
	 * @param mod
	 * 	the modifiers
	 * @param flag
	 * 	add or remove
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static void setModifier(@NonNull Field field, int mod, boolean flag)
	{
		try
		{
			field.setAccessible(true);
			int modifiers = field.getModifiers();
			if (flag)
			{
				modifiers |= mod;
			}
			else
			{
				modifiers &= ~mod;
			}
			modifiersField.setInt(field, modifiers);
		}
		catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void setAssignable(@NonNull Field field)
	{
		try
		{
			field.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifiers.FINAL);
		}
		catch (Exception ignored)
		{
		}
	}

	// Fields

	public static @NonNull <T> T[] getStaticObjects(@NonNull Class clazz, @NonNull Class<T> fieldType, boolean subtypes)
	{
		return getObjects(clazz, null, fieldType, subtypes);
	}

	public static @NonNull <T> T[] getObjects(@NonNull Class clazz, Object instance, @NonNull Class<T> fieldType,
		boolean subtypes)
	{
		List<T> list = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields)
		{
			try
			{
				Class c = field.getType();
				Object o = field.get(instance);
				if (c == fieldType || subtypes && fieldType.isAssignableFrom(c))
				{
					list.add((T) o);
				}
			}
			catch (Exception ignored)
			{
			}
		}

		return list.toArray((T[]) Array.newInstance(fieldType, 0));
	}

	// Fields

	/**
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with the name {@code name}.
	 *
	 * @param clazz
	 * 	the clazz
	 * @param name
	 * 	the field name
	 *
	 * @return the field
	 */
	public static Field getField(@NonNull Class clazz, @NonNull String name)
	{
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields)
		{
			if (name.equals(field.getName()))
			{
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with a name contained in {@code fieldNames}.
	 *
	 * @param clazz
	 * 	the clazz
	 * @param fieldNames
	 * 	the possible field names
	 *
	 * @return the field
	 */
	public static Field getField(@NonNull Class clazz, @NonNull String... fieldNames)
	{
		Field[] fields = clazz.getDeclaredFields();
		for (String fieldName : fieldNames)
		{
			for (Field field : fields)
			{
				if (fieldName.equals(field.getName()))
				{
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with the field ID {@code fieldID}
	 *
	 * @param clazz
	 * 	the clazz
	 * @param fieldID
	 * 	the field ID
	 *
	 * @return the field
	 */
	public static Field getField(@NonNull Class clazz, int fieldID)
	{
		return clazz.getDeclaredFields()[fieldID];
	}

	// Field getters

	// Reference

	public static @Nullable <T, R> R getStaticValue(@NonNull Class<? super T> clazz, String... fieldNames)
	{
		return getValue(clazz, null, fieldNames);
	}

	public static @Nullable <T, R> R getValue(@NonNull T instance, String... fieldNames)
	{
		return getValue((Class<T>) instance.getClass(), instance, fieldNames);
	}

	public static @Nullable <T, R> R getValue(@NonNull Class<? super T> clazz, T instance, String... fieldNames)
	{
		Field f = getField(clazz, fieldNames);
		return getValue(f, instance);
	}

	// Field ID

	public static @Nullable <T, R> R getStaticValue(@NonNull Class<? super T> clazz, int fieldID)
	{
		return getValue(clazz, null, fieldID);
	}

	public static @Nullable <T, R> R getValue(@NonNull T instance, int fieldID)
	{
		return getValue((Class<? super T>) instance.getClass(), instance, fieldID);
	}

	public static @Nullable <T, R> R getValue(@NonNull Class<? super T> clazz, T instance, int fieldID)
	{
		Field f = getField(clazz, fieldID);
		return getValue(f, instance);
	}

	/**
	 * Directly gets the value of the given {@link Field} on the given {@link Object} {@code instance}.
	 *
	 * @param field
	 * 	the field to get
	 * @param instance
	 * 	the instance
	 *
	 * @return the value
	 */
	public static <R> R getValue(@NonNull Field field, Object instance)
	{
		try
		{
			field.setAccessible(true);
			return (R) field.get(instance);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	// Field setters

	// Reference

	public static <T, V> void setStaticValue(@NonNull Class<? super T> clazz, V value, String... fieldNames)
	{
		setValue(clazz, null, value, fieldNames);
	}

	public static <T, V> void setValue(@NonNull T instance, V value, String... fieldNames)
	{
		setValue((Class<? super T>) instance.getClass(), instance, value, fieldNames);
	}

	public static <T, V> void setValue(@NonNull Class<? super T> clazz, T instance, V value, String... fieldNames)
	{
		Field f = getField(clazz, fieldNames);
		setField(f, instance, value);
	}

	// Field ID

	public static <T, V> void setStaticValue(@NonNull Class<? super T> clazz, V value, int fieldID)
	{
		setValue(clazz, null, value, fieldID);
	}

	public static <T, V> void setValue(@NonNull T instance, V value, int fieldID)
	{
		setValue((Class<? super T>) instance.getClass(), instance, value, fieldID);
	}

	public static <T, V> void setValue(@NonNull Class<? super T> clazz, T instance, V value, int fieldID)
	{
		Field f = getField(clazz, fieldID);
		setField(f, instance, value);
	}

	/**
	 * Directly sets the value of the given {@link Field} on the given {@link Object} {@code instance} to the given
	 * {@link Object} {@code value} .
	 *
	 * @param field
	 * 	the field to set
	 * @param instance
	 * 	the instance
	 * @param value
	 * 	the new value
	 */
	public static <T, V> void setField(@NonNull Field field, T instance, V value)
	{
		try
		{
			field.setAccessible(true);
			field.set(instance, value);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
