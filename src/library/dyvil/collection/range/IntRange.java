package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilName;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromTuple
@Immutable
public interface IntRange extends Range<Integer>
{
	@DyvilName("apply")
	static IntRange closed(int from, int to)
	{
		return new dyvil.collection.range.closed.IntRange(from, to);
	}

	@DyvilName("apply")
	static IntRange halfOpen(int from, int toExclusive)
	{
		return new dyvil.collection.range.halfopen.IntRange(from, toExclusive);
	}

	@Override
	IntRange asClosed();

	@Override
	IntRange asHalfOpen();

	boolean contains(int value);

	@Override
	default boolean contains(Object value)
	{
		return value instanceof Number && this.contains(((Number) value).intValue());
	}

	@Override
	default Integer[] toArray()
	{
		final Integer[] result = new Integer[this.size()];
		this.toArray(0, result);
		return result;
	}

	int[] toIntArray();

	@Override
	IntRange copy();
}
