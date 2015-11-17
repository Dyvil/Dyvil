package dyvil.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;

import dyvil.annotation._internal.infix;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;

public class FieldReflection
{
	private static final Field modifiersField;
	
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
	 * Adds the modifiers {@code mod} to the given {@link Field} {@code field}
	 * if {@code flag} is true, and removed them otherwise.
	 * 
	 * @param field
	 *            the field
	 * @param mod
	 *            the modifiers
	 * @param flag
	 *            add or remove
	 */
	public static @infix void setModifier(Field field, int mod, boolean flag)
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
	
	public static @infix void setAssignable(Field field)
	{
		try
		{
			field.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifiers.FINAL);
		}
		catch (Exception ex)
		{
		}
	}
	
	// Fields
	
	public static <T> T[] getStaticObjects(Class clazz, Class<T> fieldType, boolean subtypes)
	{
		return getObjects(clazz, null, fieldType, subtypes);
	}
	
	public static <T> T[] getObjects(Class clazz, Object instance, Class<T> fieldType, boolean subtypes)
	{
		List list = new ArrayList();
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields)
		{
			try
			{
				Class c = field.getType();
				Object o = field.get(instance);
				if (c == fieldType || subtypes && fieldType.isAssignableFrom(c))
				{
					list.add(o);
				}
			}
			catch (Exception ex)
			{
			}
		}
		
		return (T[]) list.toArray();
	}
	
	// Fields
	
	/**
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with
	 * the name {@code name}.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param name
	 *            the field name
	 * @return the field
	 */
	public static Field getField(Class clazz, String name)
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
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with a
	 * name contained in {@code fieldNames}.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param fieldNames
	 *            the possible field names
	 * @return the field
	 */
	public static Field getField(Class clazz, String... fieldNames)
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
		System.err.println(new NoSuchFieldException("Field not found! (Class: " + clazz + "; Expected field names: " + Arrays.toString(fieldNames)));
		return null;
	}
	
	/**
	 * Returns the {@link Field} of the given {@link Class} {@code clazz} with
	 * the field ID {@code fieldID}
	 * 
	 * @param clazz
	 *            the clazz
	 * @param fieldID
	 *            the field ID
	 * @return the field
	 */
	public static Field getField(Class clazz, int fieldID)
	{
		return clazz.getDeclaredFields()[fieldID];
	}
	
	// Field getters
	
	// Reference
	
	public static <T, R> R getStaticValue(Class<? super T> clazz, String... fieldNames)
	{
		return getValue(clazz, null, fieldNames);
	}
	
	public static <T, R> R getValue(T instance, String... fieldNames)
	{
		return getValue((Class<T>) instance.getClass(), instance, fieldNames);
	}
	
	public static <T, R> R getValue(Class<? super T> clazz, T instance, String... fieldNames)
	{
		Field f = getField(clazz, fieldNames);
		return getValue(f, instance);
	}
	
	// Field ID
	
	public static <T, R> R getStaticValue(Class<? super T> clazz, int fieldID)
	{
		return getValue(clazz, null, fieldID);
	}
	
	public static <T, R> R getValue(T instance, int fieldID)
	{
		return getValue((Class<? super T>) instance.getClass(), instance, fieldID);
	}
	
	public static <T, R> R getValue(Class<? super T> clazz, T instance, int fieldID)
	{
		Field f = getField(clazz, fieldID);
		return getValue(f, instance);
	}
	
	/**
	 * Directly gets the value of the given {@link Field} on the given
	 * {@link Object} {@code instance}.
	 * 
	 * @param field
	 *            the field to get
	 * @param instance
	 *            the instance
	 * @return the value
	 */
	public static <R> R getValue(Field field, Object instance)
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
	
	public static <T, V> void setStaticValue(Class<? super T> clazz, V value, String... fieldNames)
	{
		setValue(clazz, null, value, fieldNames);
	}
	
	public static <T, V> void setValue(T instance, V value, String... fieldNames)
	{
		setValue((Class<? super T>) instance.getClass(), instance, value, fieldNames);
	}
	
	public static <T, V> void setValue(Class<? super T> clazz, T instance, V value, String... fieldNames)
	{
		Field f = getField(clazz, fieldNames);
		setField(f, instance, value);
	}
	
	// Field ID
	
	public static <T, V> void setStaticValue(Class<? super T> clazz, V value, int fieldID)
	{
		setValue(clazz, null, value, fieldID);
	}
	
	public static <T, V> void setValue(T instance, V value, int fieldID)
	{
		setValue((Class<? super T>) instance.getClass(), instance, value, fieldID);
	}
	
	public static <T, V> void setValue(Class<? super T> clazz, T instance, V value, int fieldID)
	{
		Field f = getField(clazz, fieldID);
		setField(f, instance, value);
	}
	
	/**
	 * Directly sets the value of the given {@link Field} on the given
	 * {@link Object} {@code instance} to the given {@link Object} {@code value}
	 * .
	 * 
	 * @param field
	 *            the field to set
	 * @param instance
	 *            the instance
	 * @param value
	 *            the new value
	 */
	public static <T, V> void setField(Field field, T instance, V value)
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
