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
	static Int apply(int v)
	{
		return Int.apply(v);
	}
	
	static Long apply(long v)
	{
		return Long.apply(v);
	}
	
	static Float apply(float v)
	{
		return Float.apply(v);
	}
	
	static Double apply(double v)
	{
		return Double.apply(v);
	}
	
	// Primitive value getters
	
	byte byteValue();
	
	short shortValue();
	
	char charValue();
	
	int intValue();
	
	long longValue();
	
	float floatValue();
	
	double doubleValue();
	
	Number $plus();
	
	Number $minus();
	
	@Override
	boolean $eq$eq(Number v);
	
	@Override
	boolean $bang$eq(Number v);
	
	@Override
	boolean $lt(Number v);
	
	@Override
	boolean $lt$eq(Number v);
	
	@Override
	boolean $gt(Number v);
	
	@Override
	boolean $gt$eq(Number v);
	
	Number $plus(Number v);
	
	Number $minus(Number v);
	
	Number $times(Number v);
	
	Number $div(Number v);
	
	Number $percent(Number v);
	
	@Override
	Number next();
	
	@Override
	Number previous();
	
	@Override
	int compareTo(Number o);
	
	@Override
	default int distanceTo(Number o)
	{
		return o.intValue() - this.intValue();
	}
	
	// String representations
	
	@Override
	java.lang.String toString();
}
