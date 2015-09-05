package dyvil.lang;

import dyvil.lang.literal.DoubleConvertible;
import dyvil.lang.literal.FloatConvertible;
import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;

@IntConvertible
@LongConvertible
@FloatConvertible
@DoubleConvertible
public interface Number extends Ordered<Number>
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
	
	// Unary operators
	
	public Number $plus();
	
	public Number $minus();
	
	// byte operators
	
	public boolean $eq$eq(byte v);
	
	public boolean $bang$eq(byte v);
	
	public boolean $lt(byte v);
	
	public boolean $lt$eq(byte v);
	
	public boolean $gt(byte v);
	
	public boolean $gt$eq(byte v);
	
	public Number $plus(byte v);
	
	public Number $minus(byte v);
	
	public Number $times(byte v);
	
	public Number $div(byte v);
	
	public Number $percent(byte v);
	
	// short operators
	
	public boolean $eq$eq(short v);
	
	public boolean $bang$eq(short v);
	
	public boolean $lt(short v);
	
	public boolean $lt$eq(short v);
	
	public boolean $gt(short v);
	
	public boolean $gt$eq(short v);
	
	public Number $plus(short v);
	
	public Number $minus(short v);
	
	public Number $times(short v);
	
	public Number $div(short v);
	
	public Number $percent(short v);
	
	// char operators
	
	public boolean $eq$eq(char v);
	
	public boolean $bang$eq(char v);
	
	public boolean $lt(char v);
	
	public boolean $lt$eq(char v);
	
	public boolean $gt(char v);
	
	public boolean $gt$eq(char v);
	
	public Number $plus(char v);
	
	public Number $minus(char v);
	
	public Number $times(char v);
	
	public Number $div(char v);
	
	public Number $percent(char v);
	
	// int operators
	
	public boolean $eq$eq(int v);
	
	public boolean $bang$eq(int v);
	
	public boolean $lt(int v);
	
	public boolean $lt$eq(int v);
	
	public boolean $gt(int v);
	
	public boolean $gt$eq(int v);
	
	public Number $plus(int v);
	
	public Number $minus(int v);
	
	public Number $times(int v);
	
	public Number $div(int v);
	
	public Number $percent(int v);
	
	// long operators
	
	public boolean $eq$eq(long v);
	
	public boolean $bang$eq(long v);
	
	public boolean $lt(long v);
	
	public boolean $lt$eq(long v);
	
	public boolean $gt(long v);
	
	public boolean $gt$eq(long v);
	
	public Number $plus(long v);
	
	public Number $minus(long v);
	
	public Number $times(long v);
	
	public Number $div(long v);
	
	public Number $percent(long v);
	
	// float operators
	
	public boolean $eq$eq(float v);
	
	public boolean $bang$eq(float v);
	
	public boolean $lt(float v);
	
	public boolean $lt$eq(float v);
	
	public boolean $gt(float v);
	
	public boolean $gt$eq(float v);
	
	public Number $plus(float v);
	
	public Number $minus(float v);
	
	public Number $times(float v);
	
	public Number $div(float v);
	
	public Number $percent(float v);
	
	// double operators
	
	public boolean $eq$eq(double v);
	
	public boolean $bang$eq(double v);
	
	public boolean $lt(double v);
	
	public boolean $lt$eq(double v);
	
	public boolean $gt(double v);
	
	public boolean $gt$eq(double v);
	
	public Number $plus(double v);
	
	public Number $minus(double v);
	
	public Number $times(double v);
	
	public Number $div(double v);
	
	public Number $percent(double v);
	
	// Number operators
	
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
