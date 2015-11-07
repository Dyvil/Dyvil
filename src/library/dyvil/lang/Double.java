package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.DoubleConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

import sun.misc.FloatingDecimal;

@DoubleConvertible
public class Double implements Number, Serializable
{
	private static final long serialVersionUID = 4764381743913068148L;
	
	public static final double	min					= java.lang.Double.MIN_VALUE;
	public static final double	max					= java.lang.Double.MAX_VALUE;
	public static final double	NaN					= java.lang.Double.NaN;
	public static final double	infinity			= java.lang.Double.POSITIVE_INFINITY;
	public static final double	negative_infinity	= java.lang.Double.NEGATIVE_INFINITY;
	public static final byte	size				= java.lang.Double.SIZE;
	
	protected double value;
	
	public static Double apply(double v)
	{
		return new Double(v);
	}
	
	public static @infix double unapply(Double v)
	{
		return v == null ? 0D : v.value;
	}
	
	protected Double(double value)
	{
		this.value = value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2L })
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, D2F })
	public float floatValue()
	{
		return (float) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0 })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ LOAD_0 })
	public @prefix Double $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ LOAD_0, DNEG })
	public @prefix Double $minus()
	{
		return Double.apply(-this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public Double $plus(byte v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public Double $minus(byte v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public Double $times(byte v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public Double $div(byte v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public Double $percent(byte v)
	{
		return Double.apply(this.value % v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public Double $plus(short v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public Double $minus(short v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public Double $times(short v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public Double $div(short v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public Double $percent(short v)
	{
		return Double.apply(this.value % v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public Double $plus(char v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public Double $minus(char v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public Double $times(char v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public Double $div(char v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public Double $percent(char v)
	{
		return Double.apply(this.value % v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public Double $plus(int v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public Double $minus(int v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public Double $times(int v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public Double $div(int v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public Double $percent(int v)
	{
		return Double.apply(this.value % v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DADD })
	public Double $plus(long v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DSUB })
	public Double $minus(long v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DMUL })
	public Double $times(long v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DREM })
	public Double $percent(long v)
	{
		return Double.apply(this.value % v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DADD })
	public Double $plus(float v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DSUB })
	public Double $minus(float v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DMUL })
	public Double $times(float v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DDIV })
	public Double $div(float v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DREM })
	public Double $percent(float v)
	{
		return Double.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.doubleValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.doubleValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.doubleValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.doubleValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.doubleValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.doubleValue();
	}
	
	@Override
	public Double $plus(Number v)
	{
		return Double.apply(this.value + v.doubleValue());
	}
	
	@Override
	public Double $minus(Number v)
	{
		return Double.apply(this.value - v.doubleValue());
	}
	
	@Override
	public Double $times(Number v)
	{
		return Double.apply(this.value * v.doubleValue());
	}
	
	@Override
	public Double $div(Number v)
	{
		return Double.apply(this.value / v.doubleValue());
	}
	
	@Override
	public Double $percent(Number v)
	{
		return Double.apply(this.value % v.doubleValue());
	}
	
	@Override
	public int compareTo(Number o)
	{
		return java.lang.Double.compare(this.value, o.doubleValue());
	}
	
	@Override
	public Double next()
	{
		return Double.apply(this.value + 1D);
	}
	
	@Override
	public Double previous()
	{
		return Double.apply(this.value - 1D);
	}
	
	// Object methods
	
	public static @infix @inline String toString(double value)
	{
		return FloatingDecimal.toJavaFormatString(value);
	}
	
	public static @infix @inline String toHexString(double value)
	{
		return java.lang.Double.toHexString(value);
	}
	
	@Override
	public String toString()
	{
		return FloatingDecimal.toJavaFormatString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		long bits = java.lang.Double.doubleToLongBits(this.value);
		return (int) (bits ^ bits >>> 32);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Number))
		{
			return false;
		}
		Number other = (Number) obj;
		return this.value == other.doubleValue();
	}
}
