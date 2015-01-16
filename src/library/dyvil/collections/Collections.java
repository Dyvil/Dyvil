package dyvil.collections;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The class CSCollections.
 * <p>
 * This class adds several collection utils.
 * 
 * @author Clashsoft
 */
public class Collections
{
	/**
	 * Creates a new list from the given {@code array}
	 * 
	 * @see List
	 * @see ArrayList
	 * @param array
	 *            the array
	 * @return the ArrayList representing the array
	 */
	public static <T> List<T> create(T... array)
	{
		List<T> list = new ArrayList<T>(array.length);
		for (T t : array)
		{
			list.add(t);
		}
		return list;
	}
	
	/**
	 * Creates a new set from the given {@code array}
	 * 
	 * @see Set
	 * @see HashSet
	 * @param array
	 *            the array
	 * @return the HashSet representing the array
	 */
	public static <T> Set<T> createSet(T... array)
	{
		Set<T> set = new HashSet<T>(array.length);
		for (T t : array)
		{
			set.add(t);
		}
		return set;
	}
	
	/**
	 * Returns the component type of a collection
	 * 
	 * @see Collections#tempCollection
	 * @param collection
	 *            the collection
	 * @return the component type class
	 */
	public static Class getComponentType(Collection collection)
	{
		Class type = null;
		for (Object o : collection)
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
	 * Splits a list into sub-lists of size {@code maxLength}
	 * 
	 * @param list
	 *            the list
	 * @param maxLength
	 *            the maximum length
	 * @return the list of lists
	 */
	public static <T> List<List<T>> split(List<T> list, int maxLength)
	{
		int len = list.size();
		int arrays = (int) Math.ceil((float) len / maxLength);
		List<List<T>> ret = new ArrayList(arrays);
		
		for (int i = 0; i < arrays; i++)
		{
			List<T> sublist = new ArrayList();
			for (int j = 0; j < maxLength; j++)
			{
				int k = j + i * maxLength;
				if (k >= list.size())
				{
					sublist.add(list.get(k));
				}
			}
			ret.add(sublist);
		}
		return ret;
	}
	
	/**
	 * Concatenates two lists.
	 * 
	 * @param list1
	 *            the first list
	 * @param list2
	 *            the second list
	 * @return the list
	 */
	public static <T> List<T> concat(List<T> list1, List<T> list2)
	{
		List<T> ret = new ArrayList<T>(list1.size() + list2.size());
		ret.addAll(list1);
		ret.addAll(list2);
		return ret;
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
	 * Creates a new {@link ArrayList} from the given array.
	 * 
	 * @see CSArrays#asList(Object...)
	 * @param array
	 *            the array
	 * @return the array list
	 */
	public static <T> List<T> fromArray(T... array)
	{
		return Arrays.asList(array);
	}
	
	/**
	 * Creates an array from the given {@link Collection} {@code collection}
	 * 
	 * @see Collections#toArray(Class, Collection)
	 * @param collection
	 *            the collection
	 * @return the array
	 */
	public static <T> T[] toArray(Collection<T> collection)
	{
		return toArray(getComponentType(collection), collection);
	}
	
	/**
	 * Creates an array from the given {@link Collection} {@code collection}
	 * 
	 * @param type
	 *            the expected type
	 * @param collection
	 *            the collection
	 * @return the array
	 */
	public static <T> T[] toArray(Class type, Collection<T> collection)
	{
		T[] array = (T[]) Array.newInstance(type, collection.size());
		collection.toArray(array);
		return array;
	}
	
	/**
	 * Returns the first index of the given {@link Object} {@code object} in the
	 * given {@link List} {@code list}.
	 * 
	 * @param list
	 *            the list
	 * @param object
	 *            the object
	 * @return the first index of the object in the list
	 */
	public static <T> int indexOf(List<T> list, T object)
	{
		return indexOf(list, 0, object);
	}
	
	/**
	 * Returns the first index of the given {@link Object} {@code object} in the
	 * given {@link List} {@code list}, starting from {@code start}.
	 * 
	 * @param list
	 *            the list
	 * @param start
	 *            the index to start from
	 * @param object
	 *            the object
	 * @return the first index of the object in the array
	 */
	public static <T> int indexOf(List<T> list, int start, T object)
	{
		for (int i = start; i < list.size(); i++)
		{
			if (Objects.equals(object, list.get(i)))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the last index of the given {@link Object} {@code object} in the
	 * given {@link List} {@code list}.
	 * 
	 * @param list
	 *            the list
	 * @param object
	 *            the object
	 * @return the last index of the object in the array
	 */
	public static <T> int lastIndexOf(List<T> list, T object)
	{
		return lastIndexOf(list, list.size() - 1, object);
	}
	
	/**
	 * Returns the first index of the given {@link Object} {@code object} in the
	 * given {@link List} {@code list}, starting from the given {@code start}.
	 * 
	 * @param list
	 *            the list
	 * @param start
	 *            the index to start from
	 * @param object
	 *            the object
	 * @return the last index of the object in the array
	 */
	public static <T> int lastIndexOf(List<T> list, int start, T object)
	{
		for (int i = start; i >= 0; i--)
		{
			if (Objects.equals(object, list.get(i)))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the first index of any of the given {@link Object Objects}
	 * {@code objects} in the given {@link List} {@code list}.
	 * 
	 * @param list
	 *            the list
	 * @param objects
	 *            the objects
	 * @return the first index of the object in the list
	 */
	public static <T> int indexOfAny(List<T> list, T... objects)
	{
		for (T object : objects)
		{
			int index = indexOf(list, object);
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the last index of any of the given {@link Object Objects}
	 * {@code objects} in the given {@link List} {@code list}.
	 * 
	 * @param list
	 *            the list
	 * @param objects
	 *            the objects
	 * @return the last index of the object in the list
	 */
	public static <T> int lastIndexOfAny(List<T> list, T... objects)
	{
		for (T object : objects)
		{
			int index = lastIndexOf(list, object);
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
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
	public static <T> boolean contains(Collection<T> collection, T object)
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
	public static <T> boolean containsAny(Collection<T> collection, T... objects)
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
	public static <T> boolean containsAll(Collection<T> collection, T... objects)
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
