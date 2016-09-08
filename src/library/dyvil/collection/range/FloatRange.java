package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilName;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

@LiteralConvertible.FromTuple
@Immutable
public interface FloatRange extends Range<Float>
{
	@DyvilName("apply")
	static FloatRange closed(float from, float to)
	{
		return new dyvil.collection.range.closed.FloatRange(from, to);
	}

	@DyvilName("apply")
	static FloatRange halfOpen(float from, float toExclusive)
	{
		return new dyvil.collection.range.halfopen.FloatRange(from, toExclusive);
	}

	@Override
	FloatRange asClosed();

	@Override
	FloatRange asHalfOpen();

	boolean contains(float value);

	@Override
	default boolean contains(Object value)
	{
		return value instanceof Number && this.contains(((Number) value).floatValue());
	}

	@Override
	default Float[] toArray()
	{
		final Float[] result = new Float[this.size()];
		this.toArray(0, result);
		return result;
	}

	float[] toFloatArray();

	@Override
	FloatRange copy();
}
