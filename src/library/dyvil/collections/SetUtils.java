package dyvil.collections;

import java.util.HashSet;
import java.util.Set;

import dyvil.lang.annotation.Utility;
import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;

@Utility(Set.class)
public interface SetUtils extends CollectionUtils
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
	
	public static @infix @inline <T> Set<T> $amp(Set<T> a, Set<?> b)
	{
		Set<T> set = new HashSet(a);
		set.retainAll(set);
		return set;
	}
	
	public static @infix @inline <T> void $amp$eq(Set<T> a, Set<?> b)
	{
		a.retainAll(b);
	}
	
	public static @infix @inline <T> Set<T> $bar(Set<T> a, Set<? extends T> b)
	{
		Set<T> set = new HashSet(a);
		set.addAll(set);
		return set;
	}
	
	public static @infix @inline <T> void $bar$eq(Set<T> a, Set<? extends T> b)
	{
		a.addAll(b);
	}
	
	public static @infix @inline <T> Set<T> $minus(Set<T> a, Set<? extends T> b)
	{
		Set<T> set = new HashSet(a);
		set.removeAll(set);
		return set;
	}
	
	public static @infix @inline <T> void $minus$eq(Set<T> a, Set<? extends T> b)
	{
		a.removeAll(b);
	}
}
