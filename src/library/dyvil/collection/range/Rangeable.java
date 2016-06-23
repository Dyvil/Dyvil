package dyvil.collection.range;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.reflect.Modifiers;

public interface Rangeable<T extends Rangeable<T>> extends Comparable<T>
{
	T next();
	
	T previous();
	
	int distanceTo(T o);
	
	@Override
	int compareTo(T o);

	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T extends Rangeable<T>> Range<T> $dot$dot(T start, T end)
	{
		return Range.apply(start, end);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T extends Rangeable<T>> Range<T> $dot$dot$lt(T start, T end)
	{
		return Range.halfOpen(start, end);
	}
}
