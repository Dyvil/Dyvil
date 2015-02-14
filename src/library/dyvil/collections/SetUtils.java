package dyvil.collections;

import java.util.HashSet;
import java.util.Set;

import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;

public interface SetUtils extends Collections
{
	public static @infix @inline <T> Set<T> toSet(T... array)
	{
		Set<T> set = new HashSet<T>(array.length);
		for (T t : array)
		{
			set.add(t);
		}
		return set;
	}
}
