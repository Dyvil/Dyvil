package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilName;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromTuple
@Immutable
public interface LongRange extends Range<Long>
{
	@DyvilName("apply")
	static LongRange closed(long from, long to)
	{
		return new dyvil.collection.range.closed.LongRange(from, to);
	}

	@DyvilName("apply")
	static LongRange halfOpen(long from, long toExclusive)
	{
		return new dyvil.collection.range.halfopen.LongRange(from, toExclusive);
	}

	@Override
	LongRange asClosed();

	@Override
	LongRange asHalfOpen();

	boolean contains(long value);

	@Override
	default boolean contains(Object value)
	{
		return value instanceof Number && this.contains(((Number) value).longValue());
	}

	@Override
	default Long[] toArray()
	{
		final Long[] result = new Long[this.size()];
		this.toArray(0, result);
		return result;
	}

	long[] toLongArray();

	@Override
	LongRange copy();
}
