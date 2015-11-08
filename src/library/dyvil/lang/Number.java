package dyvil.lang;

import dyvil.lang.literal.DoubleConvertible;
import dyvil.lang.literal.FloatConvertible;
import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;

@IntConvertible
@LongConvertible
@FloatConvertible
@DoubleConvertible
public interface Number extends Rangeable<Number>
{
	public static Int apply(int v)
	{
		return Int.apply(v);
	}
	
	public static Long apply(long v)
	{
		return Long.apply(v);
	}
	
	public static Float apply(float v)
	{
		return Float.apply(v);
	}
	
	public static Double apply(double v)
	{
		return Double.apply(v);
	}
	
	// Primitive value getters
	
	public byte byteValue();
	
	public short shortValue();
	
	public char charValue();
	
	public int intValue();
	
	public long longValue();
	
	public float floatValue();
	
	public double doubleValue();
	
	public Number $plus();
	
	public Number $minus();
	
	@Override
	public boolean $eq$eq(Number v);
	
	@Override
	public boolean $bang$eq(Number v);
	
	@Override
	public boolean $lt(Number v);
	
	@Override
	public boolean $lt$eq(Number v);
	
	@Override
	public boolean $gt(Number v);
	
	@Override
	public boolean $gt$eq(Number v);
	
	public Number $plus(Number v);
	
	public Number $minus(Number v);
	
	public Number $times(Number v);
	
	public Number $div(Number v);
	
	public Number $percent(Number v);
	
	@Override
	public Number next();
	
	@Override
	public Number previous();
	
	@Override
	public int compareTo(Number o);
	
	@Override
	public default int distanceTo(Number o)
	{
		return o.intValue() - this.intValue();
	}
	
	// String representations
	
	@Override
	public java.lang.String toString();
}
