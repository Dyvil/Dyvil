package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilName;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromTuple
@Immutable
public interface DoubleRange extends Range<Double>
{
	@DyvilName("apply")
	static DoubleRange closed(double from, double to)
	{
		return new dyvil.collection.range.closed.DoubleRange(from, to);
	}

	@DyvilName("apply")
	static DoubleRange halfOpen(double from, double toExclusive)
	{
		return new dyvil.collection.range.halfopen.DoubleRange(from, toExclusive);
	}

	@Override
	DoubleRange asClosed();

	@Override
	DoubleRange asHalfOpen();

	boolean contains(double value);

	@Override
	default boolean contains(Object value)
	{
		return value instanceof Number && this.contains(((Number) value).doubleValue());
	}

	@Override
	default Double[] toArray()
	{
		final Double[] result = new Double[this.size()];
		this.toArray(0, result);
		return result;
	}

	double[] toDoubleArray();

	@Override
	DoubleRange copy();
}
