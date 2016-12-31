package dyvil.collection.range;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Range;
import dyvil.reflect.Modifiers;

public interface Rangeable<T extends Rangeable<T>> extends Comparable<T>
{
	@NonNull T next();

	@NonNull T previous();

	int distanceTo(T other);

	@Override
	int compareTo(@NonNull T other);

	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T extends Rangeable<T>> Range<T> $dot$dot(T from, T to)
	{
		return Range.closed(from, to);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T extends Rangeable<T>> Range<T> $dot$dot$lt(T from, T to)
	{
		return Range.halfOpen(from, to);
	}
}
