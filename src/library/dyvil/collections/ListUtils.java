package dyvil.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;

public interface ListUtils extends CollectionUtils
{
	public static @infix @inline <T> T $at(List<T> list, int index)
	{
		return list.get(index);
	}
	
	public static @infix @inline <T> List<T> toList(T... array)
	{
		return Arrays.asList(array);
	}
	
	public static @infix <T> List<T> $plus(List<T> list1, List<T> list2)
	{
		List<T> ret = new ArrayList<T>(list1.size() + list2.size());
		ret.addAll(list1);
		ret.addAll(list2);
		return ret;
	}
	
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
	 * Returns the first index of the given {@link Object} {@code object} in the
	 * given {@link List} {@code list}, starting from {@code start}.
	 * 
	 * @param list
	 *            the list
	 * @param object
	 *            the object
	 * @param start
	 *            the index to start from
	 * @return the first index of the object in the array
	 */
	public static @infix <T> int indexOf(List<T> list, T object, int start)
	{
		int len = list.size();
		for (int i = start; i < len; i++)
		{
			if (Objects.equals(object, list.get(i)))
			{
				return i;
			}
		}
		return -1;
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
			int index = list.indexOf(object);
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
			int index = list.indexOf(object);
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
	}
}
