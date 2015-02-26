package dyvil.collections;

import java.lang.reflect.Array;
import java.util.*;

import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;

public interface CollectionUtils
{
	public static @infix @inline <T> boolean $qmark(Collection<T> collection, T t)
	{
		return collection.contains(t);
	}
	
	public static @infix @inline <T> void $plus$eq(Collection<T> collection, T t)
	{
		collection.add(t);
	}
	
	public static @infix @inline <T> void $minus$eq(Collection<T> collection, T t)
	{
		collection.remove(t);
	}
	
	public static @infix <T> Class<T> getComponentType(Collection<T> collection)
	{
		Class<T> type = null;
		for (T o : collection)
		{
			Class c = o.getClass();
			if (type == null)
			{
				type = c;
			}
			else if (c.isAssignableFrom(type))
			{
				type = c;
			}
		}
		return type;
	}
	
	/**
	 * Removes all duplicates from the given {@link Collection}
	 * {@code collection} by using {@link HashSet HashSets}.
	 * 
	 * @param collection
	 *            the collection
	 * @return the collection without duplicates
	 */
	public static <T> List<T> removeDuplicatesSet(Collection<T> collection)
	{
		if (collection != null && !collection.isEmpty())
		{
			Set<T> set = new HashSet(collection);
			return new ArrayList<T>(set);
		}
		return java.util.Collections.EMPTY_LIST;
	}
	
	/**
	 * Removes all duplicates from the given {@link Collection}
	 * {@code collection}.
	 * 
	 * @param collection
	 *            the collection
	 * @return the collection without duplicates
	 */
	public static <T> List<T> removeDuplicates(Collection<T> collection)
	{
		if (collection != null && !collection.isEmpty())
		{
			List<T> result = new ArrayList<T>();
			for (T t1 : collection)
			{
				boolean duplicate = false;
				for (T t2 : result)
				{
					if (Objects.equals(t1, t2))
					{
						duplicate = true;
					}
					break;
				}
				
				if (!duplicate)
				{
					result.add(t1);
				}
			}
			
			return result;
		}
		return java.util.Collections.EMPTY_LIST;
	}
	
	/**
	 * Creates an array from the given {@link Collection} {@code collection}
	 * 
	 * @see CollectionUtils#toArray(Collection, Class)
	 * @param collection
	 *            the collection
	 * @return the array
	 */
	public static @infix <T> T[] toArray(Collection<T> collection)
	{
		return toArray(collection, getComponentType(collection));
	}
	
	/**
	 * Creates an array from the given {@link Collection} {@code collection}
	 * 
	 * @param collection
	 *            the collection
	 * @param type
	 *            the expected type
	 * @return the array
	 */
	public static @infix <T> T[] toArray(Collection<T> collection, Class type)
	{
		T[] array = (T[]) Array.newInstance(type, collection.size());
		collection.toArray(array);
		return array;
	}
	
	/**
	 * Checks if the given {@link Collection} {@code collection} contains the
	 * given {@link Object} {@code object}.
	 * 
	 * @param collection
	 *            the collection
	 * @param object
	 *            the object
	 * @return true, if the collection contains the object
	 */
	public static @infix <T> boolean contains(Collection<T> collection, T object)
	{
		for (T t : collection)
		{
			if (Objects.equals(t, object))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the given {@link Collection} {@code collection} contains the
	 * given {@link Object Object[]} {@code objects}.
	 * 
	 * @param collection
	 *            the collection
	 * @param objects
	 *            the objects
	 * @return true, if the collection contains any of the objects
	 */
	public static @infix <T> boolean containsAny(Collection<T> collection, T... objects)
	{
		for (T object : objects)
		{
			if (contains(collection, object))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the given {@link Collection} {@code collection} contains all of
	 * the given {@link Object Object[]} {@code objects}.
	 * 
	 * @param collection
	 *            the collection
	 * @param objects
	 *            the objects
	 * @return true, if the collection contains all objects
	 */
	public static @infix <T> boolean containsAll(Collection<T> collection, T... objects)
	{
		for (T object : objects)
		{
			if (!contains(collection, object))
			{
				return false;
			}
		}
		return true;
	}
}
